# 企业微信客户接入会员

## 1. 文档目的

本文定义企业微信智能客服场景下，外部联系人如何接入 `g2rain-member`：开通就绪、运行时链路、会员解析/创建、`SessionType=MEMBER` Token，以及身份可信度与异常规则。作为后续接口与业务实现依据。

本流程只识别当前租户内的会员，不创建 `passport`。会员上下文由 `g2rain-iam` 在 `MemberAuthorizeController#token` 成功后签发 `SessionType=MEMBER` 短期 Token，供企业微信智能客服模块经 Gateway 调用下游业务；该 Token **不是**员工/管理员登录，也 **不是**外部联系人浏览器登录态。

专题细节请交叉阅读（本文不重复展开）：

| 主题 | 文档 |
| --- | --- |
| IAM `decrypt`、`memberResolveCode`、`token` 签发细节 | [IAM 客服回调认证升级](../../../g2rain-iam/docs/design/wecom-customer-service-callback-verification.md) |
| Basis 三方 Suite 授权、企业–Organ 映射 | [Basis 企业微信授权](../../../g2rain-basis/docs/design/wechat-work-authorization.md) |
| 会员编号生成 | [会员编号生成规范](./member-no-generation.md) |

## 2. 相关定义

- [`member` 会员表](../../scripts/g2rain-member.sql#L21-L40)：租户内会员主体。
- [`member_identity` 会员身份表](../../scripts/g2rain-member.sql#L46-L65)：企业微信、手机号等身份入口。
- [`organ` 机构表](../../../g2rain-basis/scripts/g2rain-basis.sql#L62)：租户主体，由企微接入配置映射到 `organ_id`。

数据库与会员表使用 MySQL 8.0.13+ `utf8mb4_0900_as_cs`。会员编号、身份类型、`external_userid` 等字符串比较大小写敏感；调用方不得依赖数据库做大小写归一化。

## 3. 核心结论与拓扑

企业微信智能客服模块位于 **Gateway 外的应用层**，不得直连 Member `/internal/...`。渠道解密、短时 code 与 MEMBER Token 由 IAM 承接；Member 仍是会员数据所有者。企微回调 URL 打在客服模块，不是 IAM。

```text
企业微信客服回调（打到客服模块）
    ↓
客服模块 → IAM POST /auth/wecom/customer_service/decrypt
    ↓ organId + plainBody + memberResolveCode
    （decrypt 明文无 external_userid，不能创建会员）
    ↓
客服模块 → 企微 sync_msg
    ↓ msgid + external_userid + 白名单资料
    ↓
客服模块 → IAM POST /auth/member/token
    ↓
IAM（受信服务网络直连）→ Member resolveOrCreate
    ↓
IAM 签发或复用 SessionType=MEMBER Token
    ↓
客服模块持 MEMBER Token 经 Gateway 调下游业务
```

三类凭证不得混用：

| 凭证 | 签发方 | 用途 |
| --- | --- | --- |
| `memberResolveCode` | IAM（`decrypt` 成功时） | 短时、可复用；仅授权后续 `POST /auth/member/token`（同回调多消息可重复使用） |
| `SessionType=MEMBER` Token | IAM（`token` 成功后） | **仅**客服模块经 Gateway 调下游业务 |

## 4. 模块职责

| 模块 | 负责 | 不负责 |
| --- | --- | --- |
| 企业微信智能客服模块 | 收回调、调 IAM `decrypt`/`token`、`sync_msg`、咨询编排；持 MEMBER Token 调下游 | 直连 Member；持有回调密钥明文；改写授权状态 |
| `g2rain-iam` | 验签解密、定租户、发/验 code、在受信服务网络内直连 Member、签发 MEMBER Token | `sync_msg`；持久化 `member`；创建 `passport`；员工 Session；接收企微公网客服回调 |
| `g2rain-member` | `resolveOrCreate`、身份唯一性、状态与租户校验 | 验签；发 Token；猜租户 |
| `g2rain-basis` | Organ、企业–Organ 映射、三方 Suite 授权 `ACTIVE` 事实 | 创建会员；处理客服消息 |

客服模块是协议接入与咨询业务一体的单一模块，不再拆成独立「接入」与「业务」服务。

## 5. 平台与租户开通（一次性）

正式多租户默认 `bindMode = THIRD_PARTY`（本平台 Suite）。开通清单任一缺失则不得处理客户消息：

| 序号 | 初始化项 | 归属 | 验收 |
| --- | --- | --- | --- |
| 1 | 微信客服账号 `open_kfid` 可用 | 企微 / 运营 | 后台可见启用账号 |
| 2 | 回调 URL + echostr 验证 | 客服 + IAM | 验签解密回显正确 |
| 3 | `bindingCode` 登记且启用 | 客服 / 配置平台 | 外部不能自填 `organId` |
| 4 | 回调 Token、EncodingAESKey、期望接收方 | IAM | `CUSTOMER_SERVICE`；客服无明文密钥 |
| 5 | Organ 存在；企业标识 ↔ `organId` 映射唯一 | Basis | IAM 能稳定解析一个 `organId` |
| 6 | 本平台 Suite 授权为 `ACTIVE`（AgentID 非空） | Basis + IAM | `PENDING` / `REVOKED` / `EXPIRED` / 无记录均不合格 |
| 7 | 如需挂靠 G2Rain 应用，应用授权完备 | Basis | `application_authorization` 有效 |
| 8 | 企业微信 `access_token` 可换票 | 客服（或受信通道） | 仅 `ACTIVE` 租户 |
| 9 | 客服→IAM（`decrypt`/`token`）；IAM→Member 受信网络直连 | IAM / 平台 | 客服不得直连 Member；Member 不暴露到非受信网络 |

说明：

- 开通是接入级一次性工作，不是每位外部联系人重复执行。
- 授权变 `REVOKED` / `EXPIRED`、映射或密钥停用后，立即拒绝新消息。
- 正式环境不得用 `INTERNAL` 绕过 `ACTIVE` 门禁。
- 三方授权状态机、PermanentCode、扫码登录差异见 [Basis 授权设计](../../../g2rain-basis/docs/design/wechat-work-authorization.md) 与 [IAM 回调设计](../../../g2rain-iam/docs/design/wecom-customer-service-callback-verification.md)。

### 5.1 开通与取消授权时序（摘要）

```text
开通：Suite create_auth/change_auth → IAM upsert Basis ACTIVE
    → 建立企业–Organ 映射 → 登记客服 binding / 回调凭据 → echostr 通过
    → 验收 ACTIVE 后才接真实 kf_msg_or_event

取消：cancel_auth → IAM revoke Basis REVOKED
    → 后续 decrypt 失败，不返回 organId / code；本地 access_token 缓存失效
```

## 6. 运行时：每次回调到会员

```text
1. 客服 → IAM POST /auth/wecom/customer_service/decrypt
2. IAM（查 Basis）返回 organId、plainBody、memberResolveCode
   （无 external_userid；不创建会员；不发 MEMBER Token）
3. 解析 kf_msg_or_event → 拉取令牌、OpenKfId
4. 获取/复用企业微信 access_token（仅 ACTIVE）
5. sync_msg → msgid、external_userid、白名单资料
6. 按 msgid 幂等
7. 客服 → IAM POST /auth/member/token
     （memberResolveCode, externalUserId, profile, msgid；Client/Application DPoP）
8. IAM 先校验 DPoP，再无鉴权直连 Member resolveOrCreate（DPoP 失败不写会员）
9. IAM 签发/复用 MEMBER Token → 客服持 Token 经 Gateway 做业务
```

一次回调可拉多条消息：同一 `memberResolveCode` 可多次调用 `token`；按 `msgid` 幂等；同一 `(organId, external_userid, applicationCode)` 复用未过期 MEMBER Token。

`sync_msg` 游标由客服模块维护，不写入会员表。`access_token` / 拉取令牌不得入库 `member` / `member_identity`，不得写入日志。

### 6.1 调用 IAM `token` 前的最小就绪集

- 三方正式环境：Suite 授权仍为 `ACTIVE`
- 持有未过期 `memberResolveCode`
- `sync_msg` 已给出 `external_userid`（或可识别外部联系人的事件）
- 本条 `msgid` 幂等已满足
- `externalProfile` 仅白名单字段（昵称、头像等）

## 7. 首次与再次访问

「首次」：当前 `organId` 下无有效 `WECHAT_WORK` + `external_userid` 身份。  
「再次」：已存在有效身份。差异只在 Member 是否建档与 Token 是否新签，**不得**跳过 `decrypt` / sync_msg / 状态校验。

| 项目 | 首次 | 再次 |
| --- | --- | --- |
| IAM `decrypt` + code | 必须 | 必须 |
| sync_msg / msgid 幂等 | 必须 | 必须 |
| Member（经 IAM `token`） | 可能创建会员与身份 | 查询并校验 |
| MEMBER Token | 新签 | 按会话复用未过期 Token |
| `newMember` | 通常 `true` | `false` |

## 8. 会员解析与首次创建

### 8.1 算法

```text
标准化并校验 organId、externalUserId
        ↓
按 organ_id + WECHAT_WORK + external_userid 查询身份（含逻辑删除）
        ↓
存在且有效 → 查 member，校验 organ_id、delete_flag、status → 返回
        ↓
存在但已删除 → MEMBER_IDENTITY_DELETED，不创建
        ↓
不存在 → 同事务：生成 member.id / member_no → 写 member → 写 identity(verified=1)
        ↓
唯一键冲突 → 回滚 → 回查已有身份（避免孤立 member）
```

### 8.2 创建字段规则

1. 统一 ID 生成器生成 `member.id`；按[会员编号规范](./member-no-generation.md)生成 `member_no`。
2. `member.organ_id` = `memberResolveCode` 绑定的可信租户（不信请求体自报）。
3. 可将昵称、头像写入 `name` / `avatar`；缺失允许为空。
4. `member_identity`：`identity_type=WECHAT_WORK`，`identity_value=external_userid`，`verified=1`，`raw_profile` 仅白名单资料。
5. `member` 与 `member_identity` 同事务提交；创建必须经 Spring 代理可见的 `@Transactional` 入口（禁止同 Bean 自调用），唯一键冲突须先回滚再在事务外回查。
6. **禁止**在 `decrypt` 返回时创建会员。

企业微信身份 `verified=1` 只表示渠道可信，不表示手机号或法定实名已验证。

## 9. MEMBER Token

| 项 | 规则 |
| --- | --- |
| `SessionType` | `MEMBER` |
| 主体 | JWT claim `memberId`（写入 `BasePrincipal.memberId`；**不得**写入 `userId`）；不创建 `passport` |
| 协议 | 与员工 Token 同一使用协议（scopes、绑钥、Gateway DPoP/摘要）；总方案见 IAM `docs/design/member-token-issuance-alignment.md` |
| 下游 | 业务只读 `PrincipalContextHolder.getMemberId()`；对象级校验 organ + member；见 `docs/security/security-boundaries.md` |
| 刷新 | 允许 `refresh_token`（复核 Member 状态后重签）；禁止 `exchange_token` |
| 租户 | claims 含 code 绑定的 `organId`；须为租户类型 |
| 使用方 | 持票调用方经 Gateway（无调用方协议特例）；客服客户端 DPoP 实现可分期 |
| 禁止 | 不下发终端用户；不作员工登录；不可用 code 冒充 |
| 粒度 | 每个 `organId + external_userid + applicationCode` 会话一个短期 Token；未过期复用 |
| 签发时机 | Member resolve 成功且状态允许之后（由 IAM `token` 完成） |
| 拒绝签发 | 身份已删、会员冻结/删除、跨租户或损坏绑定 |

## 10. 身份可信级别与手机号

| 身份状态 | 能证明的内容 | 建议允许的业务 |
| --- | --- | --- |
| 企微身份已验证 | 当前租户下该外部联系人 | 普通咨询、公开信息 |
| 手机号已验证 | 会员控制已绑定手机号 | 与手机号关联的个人业务 |
| 独立实名完成 | 指定实名流程 | 退款、关键资料等高风险操作 |

手机号规则：

- 权威数据在 `member_identity(MOBILE)`；`member.mobile` 仅展示冗余。
- 首次绑定：同事务创建 `MOBILE` 身份并同步 `member.mobile`。
- 换绑：验证新号后同事务更新既有 `MOBILE` 身份与 `member.mobile`，不建第二条。
- 解绑：逻辑删除 `MOBILE` 并清空 `member.mobile`。
- 重新绑定：优先恢复本会员原 `MOBILE` 记录。
- 新号已绑定/曾绑定其他会员：禁止自动迁移，须合并或人工核验。

## 11. 会员状态

- `NORMAL`：可进入咨询，可签发/复用 MEMBER Token。
- `FROZEN`：仅通用说明；敏感操作拒绝或转人工；不签发可用于敏感业务的 Token。
- `member.delete_flag=1`：不当有效会员；禁止自动恢复；不发 Token。
- `member_identity.delete_flag=1`：历史占位，不得自动绑到其他会员；再出现返回 `MEMBER_IDENTITY_DELETED`。

身份唯一索引不含 `delete_flag` 是有意「永久占位」，不用 `IF(delete_flag=0,0,NULL)` 函数索引。

## 12. 接口契约

### 12.1 Member 内部（约定仅 IAM，经受信服务网络直连）

```text
POST /internal/wechat_work_member/resolve_or_create
resolveOrCreateWechatWorkMember(organId, externalUserId, externalProfile)
  → memberId, memberNo, memberStatus, newMember, identityVerified
```

普通 App、客服模块不得直调。该路径是领域解析/创建，不对客服暴露。Member 不校验 IAM 调用方身份，信任 IAM 传入的 `organId`；因此 Member 服务不得暴露到公网、客户端网络或其他非受信网络。

### 12.2 IAM 对外（客服模块调用）

```text
WeComCustomerServiceController#decrypt
  （包：`com.g2rain.iam.controller.wecom`；入口总览见 IAM `wecom-capability-map.md`）
POST /auth/wecom/customer_service/decrypt
  → organId, plainBody, memberResolveCode, …

MemberAuthorizeController#token
POST /auth/member/token
  入参：memberResolveCode, externalUserId, externalProfile?, msgid?
  出参：accessToken(SessionType=MEMBER), tokenExpiresAt, member 摘要
```

`memberResolveCode`：短时、可复用票据（建议 TTL 1–5 分钟），绑定 `organId` / binding；仅授权 `token`；同一 code 可多次换票（批消息），**非**单次消费。

### 12.3 输入校验（Member）

- `organId` 来自 IAM 可信上下文。
- `externalUserId` 去首尾空白后非空，保持企微原样大小写。
- `externalProfile` 禁止密钥、令牌、完整消息。
- 返回前同时检查身份与会员的 `delete_flag` 与租户一致。

### 12.4 建议业务错误

| 场景 | 语义 |
| --- | --- |
| 租户无效 | `MEMBER_ORGAN_INVALID` |
| 外部联系人标识缺失 | `MEMBER_WECHAT_WORK_IDENTITY_INVALID` |
| 身份指向会员不存在 | `MEMBER_IDENTITY_BROKEN` |
| 身份与会员租户不一致 | `MEMBER_IDENTITY_ORGAN_MISMATCH` |
| 企微身份已逻辑删除 | `MEMBER_IDENTITY_DELETED` |
| 会员已冻结 | `MEMBER_FROZEN` |
| 会员或身份已删除 | `MEMBER_NOT_FOUND` |
| 并发创建后无法回查 | `MEMBER_IDENTITY_CREATE_CONFLICT` |
| code 无效/过期 | IAM：`WECOM_MEMBER_RESOLVE_CODE_INVALID`（名称以实现为准） |

## 13. 异常与安全

- `decrypt` 失败：停止后续 sync_msg 与 `token`。
- 无法定租户：拒绝，不用默认 `organId`。
- sync_msg 失败：按企微协议重试；不提前建会员、不发 Token。
- 无 `external_userid`：不识别会员，按事件类型处理。
- 重复 `msgid`：幂等返回；可复用已有 MEMBER Token。
- code 无效：IAM 拒绝，不调 Member。
- 日志不得输出回调密钥、access_token、拉取令牌、code/Token 全文、验证码、完整客服正文。

## 14. 调用下游业务前就绪

```text
memberId 有效 + 状态允许本操作
  + 所需身份可信级别已满足
  + 客服模块持有未过期 SessionType=MEMBER Token
```

| 操作 | 最低条件 |
| --- | --- |
| 普通咨询 | 企微身份已验证 + memberId |
| 手机号关联业务 | 另需已验证 MOBILE |
| 退款 / 关键资料变更 | 按业务要求实名或增强验证 |

## 15. 实现清单

- [ ] 与 IAM 确认 `decrypt`、`memberResolveCode`、`POST /auth/member/token` 以及 IAM→Member 受信网络直连契约。
- [ ] Member `resolveOrCreate` 调用方收窄为仅 IAM。
- [ ] 定义 Member / IAM 侧 API、DTO、VO、错误码。
- [ ] 实现身份查询、首次同事务创建、唯一键冲突回查。
- [ ] 实现状态校验、已删除身份永久占位。
- [ ] 实现手机号绑定/换绑/解绑与 `member.mobile` 同步。
- [ ] `raw_profile` 白名单与日志脱敏。
- [ ] 联调：开通 ACTIVE → `decrypt` → sync_msg → `token` → MEMBER Token 复用。
- [ ] 测试：首次/再次、并发、跨租户、冻结、逻辑删除、损坏绑定、code 过期。
