# 模块与依赖边界

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

## Agent 检查清单

维护或生成项目文档时，Agent 应读取当前源码、POM、`docs/project.yaml` 和 Git Diff，动态检查：

- 根 POM 声明的 Maven 模块与文档一致。
- API 不依赖 Biz 或 Startup；Biz 依赖 API 且不依赖 Startup；Startup 依赖 Biz。
- API 源码不导入 Controller、Service、DAO、Config 或 Biz 实现包。
- 必需文档及 Markdown 相对链接有效。
- 启动类、端口、profile、命令和基础设施依赖与当前配置一致。
- 新增身份类型、内部接口或跨服务关系已经同步到架构与设计文档。

这些检查由 Agent 在任务期间执行并报告，不要求业务仓库维护专用验证脚本。

## 新模块规则

新增 Maven 模块时，同一提交必须更新 `docs/project.yaml`、本页和[模块职责](modules.md)。引入长期架构边界时，还应增加 ADR。

