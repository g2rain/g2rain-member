# API 设计规范

## 契约位置

- 可复用契约放在 `g2rain-member-api`，Controller 在 Biz 模块实现对应 `Api`。
- 写入或独立业务请求使用 `Dto` / `Request`，查询使用 `SelectDto`，响应使用 `Vo`。
- PO 和 Biz 内部 DTO 不得作为公开或跨服务响应。

## 当前 DTO 分布

| 模块 | 类型 | 用途 |
| --- | --- | --- |
| API | `MemberSelectDto`、`MemberIdentitySelectDto` | `MemberApi`、`MemberIdentityApi` 的查询条件 |
| API | `WechatWorkMemberResolveRequest`、`WechatWorkExternalProfileDto` | 企业微信受信内部契约输入 |
| Biz | `MemberDto`、`MemberIdentityDto` | Biz Controller 的新增/更新输入及 Service、Converter 内部传输 |

API 与 Biz 的 DTO 当前使用相同 Java 包名，但只有 API 模块中的类型会随 `g2rain-member-api` JAR 发布。`MemberDto`、`MemberIdentityDto` 当前不属于其他服务可复用的契约。

这种分布是有意的模块边界：

- API 模块优先发布供其他后端模块使用的查询契约。
- Biz 写入 DTO 服务于 App 经 Gateway 发起的 Member 编辑用例，不鼓励其他后端模块同步调用。
- 其他模块发生的业务事实需要改变会员数据时，优先发布领域消息，由 Member 自主消费和编辑。

如果确有场景需要把 Member 写能力发布为稳定的跨模块同步契约，应：

1. 在 API 模块定义具有明确业务语义的请求类型和 Api 方法。
2. 评估 `organId` 是否应来自可信上下文，而不是允许调用方任意提交。
3. 明确客户端可写字段，避免直接暴露生成器根据表字段创建的宽模型。
4. 补充兼容策略、调用方测试和版本升级说明。
5. 保留 Biz DTO 作为内部模型或完成迁移后移除，不能形成两个含义重叠的公开契约。
6. 说明为什么 App 调用或领域消息不能满足，并通过 ADR 记录该架构例外。

不得直接把通用 `/save`、`/update` 或 `/delete` 包装到 API 模块供其他后端模块调用。跨模块写命令必须表达具体业务语义，并由 Member 保留数据所有权和最终决策权。

## 路由与方法

- 沿用项目 snake_case 路由，例如 `/member_identity`、`/resolve_or_create`。
- 查询使用 GET，创建或复杂命令使用 POST，删除使用 DELETE；更新语义应在契约中保持一致。
- 路由表达业务资源或用例，不暴露数据库表操作细节。
- 受信接口使用 `/internal/...`，并在 OpenAPI 中隐藏，但仍必须由安全机制限制调用方。

## 输入与输出

- 输入使用 Jakarta Validation 表达必填、长度和格式，跨字段、状态与租户规则由 Service 校验。
- 返回统一使用 `Result<T>`；分页统一使用 `PageData<T>`。
- 时间、枚举、空值和分页语义应与现有 API 保持一致。
- 不返回 Token、密钥、验证码、完整外部资料或不必要的内部字段。

## 错误与兼容性

- 可预期业务失败使用稳定错误码，不把数据库异常或堆栈直接返回调用方。
- 新增字段优先保持向后兼容；删除、重命名或改变字段语义属于破坏性变更。
- 修改 `g2rain-member-api` 前列出调用方、部署顺序和版本升级要求。
- 并发冲突、已删除身份、跨租户和损坏绑定应返回可区分的业务错误。

## 内部会员解析契约

企业微信解析接口只接受：

- 已由可信上游确定的 `organId`。
- 企业微信可信接口返回的 `externalUserId`。
- 昵称、头像等允许落库的最小化资料。

不得接受回调密文、签名、Token、拉取令牌、完整客服消息或由普通客户端自行声明的租户身份。

## 变更检查

- API 模块、Controller 实现、OpenAPI 描述和调用示例保持一致。
- 参数校验、权限入口、错误码和测试同步更新。
- 破坏性变更必须有需求设计、兼容策略和必要 ADR。
