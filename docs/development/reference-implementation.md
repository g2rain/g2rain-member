# 参考实现路径

本文为 AI Coding 提供项目内的阅读路径。现有文件各自展示一种模式，不代表可以整段复制；实现仍必须以需求、架构和测试规范为准。

## 企业微信会员解析用例

按以下顺序阅读：

1. `g2rain-member-api/.../WechatWorkMemberInternalApi.java`：内部契约和路由。
2. `g2rain-member-api/.../WechatWorkMemberResolveRequest.java`：受信输入及校验。
3. `g2rain-member-biz/.../WechatWorkMemberInternalController.java`：薄 Controller 适配。
4. `g2rain-member-biz/.../WechatWorkMemberResolver.java`：领域用例接口。
5. `g2rain-member-biz/.../WechatWorkMemberResolverImpl.java`：身份查询、会员创建和冲突处理。
6. `g2rain-member-biz/.../WechatWorkMemberResolverImplTest.java`：关键业务场景测试。
7. [企业微信客户接入会员](../design/wechat-work-customer-member-onboarding.md)：开通就绪、解析创建、MEMBER Token 与异常规则。

重点观察：

- API 契约与实现模块分离。
- Controller 不承载领域逻辑。
- 外部资料先经过白名单清理。
- 查询包含逻辑删除记录，以执行历史身份占位规则。
- 创建会员和身份需要事务与唯一键冲突处理。
- 返回前校验会员、身份与请求租户一致。

## 标准 CRUD 用例

`MemberApi` → `MemberController` → `MemberService` → `MemberDao` 展示标准查询、分页、保存、删除和 MapStruct 转换路径。

该路径中的 DTO 分属两个模块：

- `MemberSelectDto`、`MemberIdentitySelectDto` 位于 API 模块，是查询接口契约。
- `MemberDto`、`MemberIdentityDto` 位于 Biz 模块，是当前 `/save` Controller、Service 和 Converter 使用的本地写入模型。

因此不能把 `com.g2rain.member.dto` 整个包视为 API 契约，也不能从其他服务依赖 Biz JAR 来获得写入 DTO。新增跨服务写入能力时，应在 API 模块设计独立请求契约。

更常见的协作方式是：查询契约留在 API 模块；编辑由 App 经 Gateway 调用 Member；非交互式变化由上游发布领域消息、Member 自主消费。不要把生成的 `MemberDto` 或 `MemberIdentityDto` 移到 API 模块作为通用远程保存接口。

生成新的 CRUD 后不能止步于模板，必须结合需求补充：

- 权限与租户边界。
- 状态和唯一性规则。
- 事务、幂等和并发处理。
- 敏感字段过滤。
- 正常、异常和回归测试。

## 会员编号纯领域规则

`MemberNoGenerator` 和 `MemberNoGeneratorTest` 展示无外部依赖的纯领域逻辑。相似的格式化、规范化和确定性规则优先放入 Domain，并使用快速单元测试覆盖。

## 新用例推荐结构

```text
API 模块
  Api / Request / SelectDto / Vo / ErrorCode
        ↓
Biz 模块
  Controller（协议适配） + 本地写入 Dto（不跨服务发布）
        ↓
  Service（用例、事务、权限、幂等）
        ↓
  Domain（纯规则） + DAO（持久化） + Converter（结构转换）
        ↓
  Unit / Integration Tests
```

AI Coding 在引用现有实现时，应先检查目标文件当前状态、测试和 Git Diff，不能假设生成代码天然满足最新规范。
