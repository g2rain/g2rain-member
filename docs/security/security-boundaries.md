# 安全与租户边界

## 调用边界

- 前端 App 通过 IAM 登录取得 Token，再经 Gateway 访问 Member 公开接口。
- 普通客户端不得直连 `/internal/...`。
- 企业微信智能客服模块位于 Gateway 外：只调 IAM `decrypt` / `token` 与企业微信；不直连 Member。
- Member `resolveOrCreate` 的约定调用方是 `g2rain-iam`；IAM 通过服务发现无鉴权直连，Member 不校验调用方身份并信任 IAM 传入的 `organId`。
- 该模式以受信服务网络为安全边界。Member 服务及其 `/internal/...` 接口不得暴露到公网、客户端网络或其他非受信网络；若网络边界发生变化，须先引入调用方鉴权。
- IAM 可签发 `SessionType=MEMBER` 短期 Token，**仅**供客服模块经 Gateway 调下游业务；不下发终端微信用户，不充当员工登录。
- `memberResolveCode` 为短时、可复用票据（非 OAuth 授权码式单次消费），与 MEMBER Token 分轨校验，不得混用；IAM→Member 直连不使用这两类凭证。
- MEMBER 可通过 `refresh_token` 续期，但 Client DPoP `acd` 须等于原 Token 绑定的 `applicationCode`，并经 IAM→Member `GET /member/active_for_token` 复核状态后重签；不得 `exchange_token` 为员工会话。
- 除已在 `docs/architecture/deviations.md` 登记的 IAM→Member 受信网络直连例外外，URL 前缀、内网地址和 OpenAPI 隐藏不能替代服务身份验证与授权；该例外的安全性由部署隔离和网络策略承担。

## 租户边界

- `organId` 必须来自可信认证或服务调用上下文，不能相信普通请求参数自行声明的租户。
- 请求、`member_identity.organ_id` 和 `member.organ_id` 必须一致。
- 默认使用数据隔离能力；绕过隔离仅限明确的受信用例，并在 Service 层补足校验。
- 跨租户不匹配必须拒绝并记录可审计但不泄密的安全事件。

## MEMBER 会话身份收口

- 经 Gateway 的 MEMBER 请求：当前会员身份只读 `PrincipalContextHolder.getMemberId()`，不新增可由 Query/Body/Path 承载的会员身份参数。
- 查询与对象操作：MEMBER 会话强制收敛到当前 `organId` + `memberId`；DTO 中的伪造 `memberId`/`organId` 须覆盖或拒绝。
- `GET /member/current` 从 Principal 取会员资料；MEMBER 会话禁止新增会员、删除会员、改绑 `member_identity`。
- 入口权限（Gateway `MemberPerm`）与对象级授权分轨：前者决定能否进 API，后者在 Service 内校验资源归属。

## 身份安全

- 企业微信身份值保持可信接口返回的大小写，仅去除首尾空白。
- 已删除身份继续占位，不得自动绑定到其他会员。
- `verified=1` 仅表示来源渠道已验证，不等同于手机号验证、实名或会员本人登录。
- 手机号换绑、解绑和恢复必须遵循身份历史与增强验证设计。

## 数据最小化

- 请求、数据库、日志和响应只处理完成用例所需字段。
- 不接收或保存密码、Token、回调密钥、EncodingAESKey、拉取令牌、验证码和完整客服消息。
- `raw_profile` 仅保存允许的昵称、头像等白名单资料。
- 错误响应不暴露 SQL、内部地址、堆栈、密钥或第三方完整响应。

## 安全变更要求

涉及鉴权、内部接口、租户上下文、身份恢复或敏感字段的变更必须：

- 创建或更新需求/设计文档。
- 明确调用者、信任来源、攻击面和拒绝路径。
- 增加越权、跨租户、重放、伪造输入和敏感信息泄露测试。
- 需要时增加 ADR，并说明兼容与部署顺序。
