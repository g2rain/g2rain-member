# 代码规范

本文根据 `g2rain-member` 当前 API、Controller、Service、DAO、MapStruct Converter、领域对象和测试整理。规范优先约束新增和修改代码，不要求为格式统一批量改写无关历史代码。

## 1. 基础与命名

- 文本使用 UTF-8；Java 使用 4 个空格，YAML 使用 2 个空格，不使用 Tab。
- 类、接口、枚举使用 UpperCamelCase；方法、参数、字段使用 lowerCamelCase；常量和枚举值使用大写下划线。
- 类型后缀表达职责：`Api`、`Controller`、`Service`、`Dao`、`Po`、`Dto`、`Request`、`Vo`、`Converter`、`Config`。
- 测试类使用“被测类型 + `Test`”，测试方法说明条件与预期结果。

## 2. 模块与包边界

```text
g2rain-member-startup → g2rain-member-biz → g2rain-member-api
```

- API 只放稳定契约，不引用实现包。
- Biz 实现契约和领域规则，不依赖 Startup。
- Startup 只负责启动、配置和运行时组装。
- Controller → Service → DAO/Domain；DAO、Converter 和 Domain 不反向调用 Controller。

## 3. API 与 Controller

- 可复用契约定义在 API 模块，Controller 实现对应接口。
- Controller 只负责路由、参数绑定、校验和结果包装；事务及业务规则位于 Service。
- 返回值统一使用 `Result<T>`，分页使用项目公共 `PageData<T>`。
- 路由沿用项目现有 snake_case，例如 `/member_identity` 和 `/resolve_or_create`。
- 受信内部接口使用 `/internal/...` 并在 OpenAPI 中隐藏；路径与隐藏不能替代真实访问控制。

## 4. 会员与身份规则

- `organId` 必须来自可信上下文，所有跨隔离读写都要显式校验会员、身份和请求租户一致。
- 企业微信 `externalUserId` 只去除首尾空白，不自行改变大小写。
- 同一租户、身份类型和身份值只能绑定一个会员；已删除身份继续占位，不自动转移。
- 创建会员与身份必须在同一事务中完成；唯一键冲突后回滚并回查既有身份。
- `memberNo` 使用 `MemberNoGenerator` 生成，不拼接手机号、机构或日期。
- `rawProfile` 仅保存白名单资料，不保存 Token、回调密钥、拉取令牌或完整消息。

## 5. Service、DAO 与转换

- Service 负责校验、状态迁移、事务、幂等、脱敏和跨 DAO 协调。
- 新主键使用平台 `IdGenerator`；可预期失败使用项目统一异常和错误码。
- DAO 只处理持久化；`WithoutIsolation` 方法必须由受信 Service 补足租户与安全校验。
- PO 不直接作为响应；常规转换使用 MapStruct，安全过滤在 Service 出口显式完成。
- 数据库结构变更同步更新 `scripts/g2rain-member.sql`，并评估其删除重建特性和迁移风险。

## 6. 配置、日志与文档

- 环境差异通过环境变量、profile、Nacos 或配置属性表达，不在 Java 中硬编码。
- 不使用 `System.out` 或 `printStackTrace`；日志不得记录密码、Token、手机号验证码或完整外部资料。
- 注释解释边界、兼容性和原因，不逐行翻译代码。
- 修改模块、身份规则、租户隔离、内部接口、运行配置或生成流程时，同步更新 `docs`；长期决策增加 ADR。

## 7. 测试与提交

- 会员编号覆盖正数、确定性和格式边界。
- 企业微信解析覆盖已有身份、首次创建、并发冲突、跨租户、冻结/删除及损坏绑定。
- 数据库或 Mapper 改动应补充集成测试；接口契约变化评估调用方兼容性。
- 提交前执行：

```bash
mvn clean verify
```

