# 故障排查

## 服务无法启动或连接数据库

- 确认 `SPRING_PROFILES_ACTIVE` 包含 `dev` 或已由目标环境提供数据源。
- 确认 MySQL 中存在 `g2rain_member`，且表结构与 `scripts/g2rain-member.sql` 对应。
- Mapper 应位于 `classpath:/mybatis/mapper/*.xml`。
- 不要在有会员数据的环境重复运行会删除并重建表的初始化脚本。

## 服务无法注册或读取 Nacos 配置

- 确认 profile 包含 `nacos`。
- 检查 `NACOS_SERVER_ADDR`、用户名、密码和 namespace。
- discovery group 应为 `g2rain`，服务名和配置 group 应为 `g2rain-member`。

## App 无法访问会员接口

- 确认 App 已从 IAM 获得用于 Gateway 的有效 Token。
- 检查 Gateway 中 Member 路由、鉴权策略和服务发现结果。
- 普通 App 不得直连 `/internal/...` 接口。

## 企业微信会员解析失败

- 确认客服模块已走 IAM `decrypt`（取得 `memberResolveCode`）→ `sync_msg` → IAM `POST /auth/member/token`，而非直连 Member。
- 确认 IAM 通过服务发现直连 Member，且 `organId` 来自 code 绑定；该调用不经过 Gateway、不使用服务凭证。
- 确认 `externalUserId` 来自 `sync_msg` 的可信响应，且没有被改变大小写。
- `MEMBER_IDENTITY_DELETED` 表示历史身份仍占位，不能自动新建或转移。
- 唯一键冲突后应回滚新建事务并回查已有身份。
- MEMBER Token 仅客服模块使用；按 `organId + external_userid + applicationCode` 会话复用。
- 详细边界见[企业微信客户接入会员](../design/wechat-work-customer-member-onboarding.md)。

## 租户数据异常

- 核对请求、`member_identity.organ_id` 和 `member.organ_id` 是否一致。
- 检查是否误用了 `WithoutIsolation` DAO 方法而没有在 Service 层显式校验租户。
- 不要通过关闭 `g2rain.data.isolation.enabled` 掩盖数据或调用链问题。

## 文档验证失败

让 Agent 结合当前源码、POM、`docs/project.yaml` 和 Git Diff 检查缺失文件、失效链接、模块差异与非法依赖。有意调整架构时，应同步更新项目元数据、文档和 ADR，不能只修改 README 摘要。
