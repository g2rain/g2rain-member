# g2rain-member Agent Instructions

本文件是 AI Coding 在本项目中的执行入口。事实来源位于 `docs`，不要在本文件重复维护完整架构和代码规范。

## 开始实现前

按顺序读取：

1. `docs/project.yaml`
2. `docs/architecture/overview.md`
3. `docs/architecture/modules.md`
4. `docs/architecture/dependencies.md`
5. `docs/development/code-conventions.md`
6. `docs/development/testing.md`
7. `docs/development/definition-of-done.md`
8. 当前需求对应的 `docs/requirements`、`docs/design` 或 `docs/decisions` 文档

涉及接口、数据库、安全、依赖或运行配置时，还必须读取相应专题规范。

## 实现约束

- 保持 `g2rain-member-startup → g2rain-member-biz → g2rain-member-api` 依赖方向。
- 跨模块同步依赖原则上只发布查询契约，不新增供其他后端模块直接调用的通用保存、更新或删除契约。
- Member 数据编辑优先由 App 经 Gateway 调用本服务写接口，或由 Member 监听其他模块发布的领域消息后自主更新；不能让其他模块越过 Member 领域规则直接编辑数据。
- Controller 只处理协议适配；业务规则、事务、幂等和租户校验放在 Service/Domain。
- 所有会员和身份数据必须保持请求、身份与会员的 `organId` 一致。
- 使用 `WithoutIsolation` DAO 方法时必须在受信 Service 中显式完成租户和访问边界校验。
- 受信内部接口的路径和 OpenAPI 隐藏不能替代服务身份与访问控制。
- 不记录或返回密码、Token、回调密钥、拉取令牌、验证码和非必要个人资料。
- 修改 API、数据库、配置、模块、依赖、生成流程或长期设计时，同步更新相关文档。
- 不向业务仓库添加仅供 Agent 使用的文档验证脚本；由 Agent 在任务中动态验证。

## 完成前

- 检查 Git Diff 或本次文件差异，排除生成器误覆盖和无关修改。
- 按 `docs/development/testing.md` 补充测试。
- 执行 `mvn clean verify`；无法执行时明确报告原因和未验证风险。
- 动态检查 Markdown 相对链接、`docs/project.yaml`、POM、启动类、端口和模块边界的一致性。
- 按 `docs/development/definition-of-done.md` 逐项确认并报告结果。
