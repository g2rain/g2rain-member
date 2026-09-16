# 模块与依赖边界

本页是中央 `java-domain-service` 模块规则在 Member 中的具体落地。公共规则以[中央 Profile 1.0.0](https://github.com/g2rain/g2rain/tree/architecture-v1.0.0/docs/architecture/profiles/java-domain-service)为准，项目偏离必须登记到[架构例外](deviations.md)。

## 允许的方向

```text
startup → biz → api
```

| 来源 | 允许依赖 | 禁止依赖 |
| --- | --- | --- |
| `g2rain-member-api` | 通用模型、Spring Web/Validation 契约 | Biz、Startup 和实现包 |
| `g2rain-member-biz` | API、持久化与平台 Starter | Startup |
| `g2rain-member-startup` | Biz、Spring Boot 运行组件 | 供下层反向依赖的领域实现 |

## 包级边界

```text
Controller → Service → DAO
                 └──→ Domain
```

- Controller 负责 HTTP 协议、校验、权限入口和结果转换。
- Service 负责事务、幂等、状态、租户一致性和身份安全约束。
- DAO 只负责持久化，不反向调用 Service 或 Controller。
- Converter 和 Domain 纯计算对象不发起数据库或网络调用。
- API 模块只发布稳定契约，不暴露 Biz 内部 DTO 或 PO。

## DTO 依赖边界

两个模块都存在 `com.g2rain.member.dto` 包，但同包名不代表同一架构层：

```text
API dto
  MemberSelectDto / MemberIdentitySelectDto
  WechatWorkMemberResolveRequest / WechatWorkExternalProfileDto
        ↓ API 契约允许引用

Biz dto
  MemberDto / MemberIdentityDto
        ↓ 只允许 Biz Controller、Service、Converter 引用
```

- 架构检查必须同时检查 Maven 模块路径和类型引用，不能只按 `com.g2rain.member.dto` 包名放行。
- API 源码不得引用 `MemberDto`、`MemberIdentityDto` 或任何位于 Biz 源码目录的类型。
- Biz 可以引用 API DTO，因为依赖方向为 `biz → api`；API 不能反向引用 Biz DTO。
- 其他服务只应依赖 `g2rain-member-api` 中明确发布的契约。若一个 Biz 写入模型需要跨服务复用，应在 API 模块定义独立请求契约，而不是让调用方依赖 Biz JAR。
- `/member/save` 和 `/member_identity/save` 当前由 Biz Controller 直接声明，使用的 `MemberDto`、`MemberIdentityDto` 属于服务实现本地输入；不能仅因它们是 HTTP 请求体就认定为已发布的 API 模块契约。

## 跨模块读写边界

模块间同步依赖主要用于查询：

```text
其他后端模块 → g2rain-member-api 查询契约 → Member
```

不推荐：

```text
其他后端模块 → 通用 save/update/delete → 修改 Member 数据
```

推荐的编辑路径：

```text
App → Gateway → Member 写用例

其他模块 → 发布领域消息 → Member 监听并自主编辑
```

- Member 拥有会员和会员身份数据，写入校验、状态迁移、事务和幂等不能分散到调用模块。
- 领域消息表达上游已经发生的业务事实，例如“已完成手机号验证”，而不是携带“更新某表某字段”的数据库操作指令。
- 消息消费者必须处理重复投递、乱序、重试、失败恢复和可观测性。
- 当前项目尚未实现跨模块消息消费者；引入消息基础设施和事件契约时必须更新运行依赖、测试、运维文档并增加必要 ADR。
- 同步跨模块写入是例外，不是默认模式；确需引入时必须定义具体业务命令，不能发布宽泛 CRUD DTO。

## Agent 检查清单

维护或生成项目文档时，Agent 应读取当前源码、POM、`docs/project.yaml` 和 Git Diff，动态检查：

- 根 POM 声明的 Maven 模块与文档一致。
- API 不依赖 Biz 或 Startup；Biz 依赖 API 且不依赖 Startup；Startup 依赖 Biz。
- API 源码不导入 Controller、Service、DAO、Config 或 Biz 实现包。
- API 源码不引用物理位于 Biz 模块的 `MemberDto`、`MemberIdentityDto`；检查 DTO 边界时以 Maven 源码路径为准。
- 必需文档及 Markdown 相对链接有效。
- 启动类、端口、profile、命令和基础设施依赖与当前配置一致。
- 新增身份类型、内部接口或跨服务关系已经同步到架构与设计文档。

这些检查由 Agent 在任务期间执行并报告，不要求业务仓库维护专用验证脚本。

## 新模块规则

新增 Maven 模块时，同一提交必须更新 `docs/project.yaml`、本页和[模块职责](modules.md)。引入长期架构边界时，还应增加 ADR。
