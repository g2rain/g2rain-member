# 企业微信智能客服联调与开通验收清单

本文汇总客服模块、Gateway 与运维侧的联调/验收项，对应 IAM《客服回调认证升级》与 Member《企业微信客户接入会员》。本清单不在客服 / Gateway 仓库实现，但阻塞上线。

关联设计：

- [IAM 客服回调认证升级](../../../g2rain-iam/docs/design/wecom-customer-service-callback-verification.md)
- [Member 企业微信客户接入会员](./wechat-work-customer-member-onboarding.md)
- [Basis 企业微信授权](../../../g2rain-basis/docs/design/wechat-work-authorization.md)

## 1. 客服模块（Gateway 外侧）

| 序号 | 验收项 | 通过标准 |
| --- | --- | --- |
| CS-1 | 企微回调 URL 打在客服模块，不打在 IAM | 公网回调仅命中客服入口 |
| CS-2 | 收到密文后只调 IAM `POST /auth/wecom/customer_service/decrypt` | 请求含 `callbackType=CUSTOMER_SERVICE`、`bindingCode`、签名参数与密文；不自填 `organId` |
| CS-3 | `decrypt` 成功后解析 `kf_msg_or_event`，再调企微 `sync_msg` | 取得 `msgid`、`external_userid`；IAM 不调 `sync_msg` |
| CS-4 | 每条需识别会员的消息调 IAM `POST /auth/member/token` | 携带 `memberResolveCode` + `externalUserId`（可选 `msgid` / 白名单 profile） |
| CS-5 | 持返回的 `SessionType=MEMBER` Token 经 Gateway 调下游 | 不把 Token 下发终端微信用户；不作员工登录 |
| CS-6 | 禁止直连 Member `/internal/wechat_work_member/**` | 代码与网络策略均无直连 |
| CS-7 | 不持有回调 Token / EncodingAESKey 明文 | 密钥仅在 IAM 配置；日志无密钥与解密全文 |
| CS-8 | 同一 `memberResolveCode` 可覆盖一次回调多消息；按 `msgid` 幂等 | 重试不重复建档；复用未过期 MEMBER Token |

## 2. Gateway 与服务网络

| 序号 | 验收项 | 通过标准 |
| --- | --- | --- |
| GW-1 | 客服 → IAM：`/auth/wecom/customer_service/decrypt`、`/auth/member/token` | 按客服/内部应用调用方认证开放；非终端用户登录；非企微直连 |
| GW-2 | IAM → Member：`/internal/wechat_work_member/**` | 不经 Gateway、无鉴权直连；Member 仅位于受信服务网络，不对公网、客户端或非受信服务开放 |
| GW-3 | 客服 → 下游业务：接受 `SessionType=MEMBER` | 与 USER / PASSPORT 分轨：JWT 写 `memberId`/`X-MEMBER-ID`，跳过 DPoP/摘要；入口权限走 DefaultPerm 且要求 `organId`+`memberId` |
| GW-4 | 拒绝伪造 `organId` 的普通客户端直达 Member 内部写接口 | 越权探测返回拒绝且不落敏感信息 |
| GW-5 | 限流与超时 | `decrypt` / `token` / `resolveOrCreate` 有明确超时与限流配置 |

## 3. 开通与运维

| 序号 | 验收项 | 通过标准 |
| --- | --- | --- |
| OPS-1 | 微信客服账号 `open_kfid` 可用 | 企微后台可见启用账号 |
| OPS-2 | 回调 URL + echostr 验证 | 客服转发 IAM 验签解密后回显正确 |
| OPS-3 | `bindingCode` 登记且启用 | IAM `g2rain.iam.wecom.customer-service.bindings` 可解析；外部不能自填 `organId` |
| OPS-4 | 客服回调 Token / EncodingAESKey / expectedReceiver | 与授权回调密钥分配置；缺失时仅禁用客服能力 |
| OPS-5 | Organ 存在；`idp_enterprise_organ` ACTIVE 唯一映射 | IAM organ resolve 稳定返回一个 `organId`；0/多条拒绝 |
| OPS-6 | 三方 Suite 授权 `ACTIVE` 且 AgentID 非空 | `PENDING` / `REVOKED` / `EXPIRED` / 无记录均不得接消息 |
| OPS-7 | （可选）G2Rain `application_authorization` | 若业务要求挂靠应用则有效 |
| OPS-8 | 企业微信 `access_token` 可换票 | 仅 ACTIVE 租户；由客服侧维护，不入库会员表 |
| OPS-9 | 取消授权后立即失效 | `cancel_auth` 后 `decrypt` 失败；本地 access_token 缓存失效 |
| OPS-10 | 日志与审计 | 无 Token / AESKey / PermanentCode / `memberResolveCode` 全文 / MEMBER Token 全文 / 解密正文 |

## 4. 端到端联调顺序

```text
1. 开通 OPS-1～OPS-8
2. echostr URL 验证（可不发 memberResolveCode）
3. 模拟 kf_msg_or_event → CS decrypt → IAM 返回 organId + plainBody + code
4. sync_msg → msgid + external_userid
5. CS → IAM POST /auth/member/token → MEMBER Token
6. 持 Token 经 Gateway 调一条下游只读接口
7. 再次消息：验证 Token 复用与 msgid 幂等
8. 造失败：停用映射 / REVOKED / 伪造 code / 冻结会员 → 均不发可用 Token
```

## 5. 本仓已交付对照（实现侧）

| 能力 | 仓库 | 状态说明 |
| --- | --- | --- |
| `POST /internal/idp/enterprise-organ/resolve` | g2rain-basis | 已实现 |
| `WeComCallbackVerifier` + `POST .../decrypt` | g2rain-iam | 已实现 |
| `POST /auth/member/token` + MEMBER Token | g2rain-iam | 已实现 |
| `resolveOrCreate` 约定调用方为 IAM | g2rain-member | 受信服务网络无鉴权直连；Member 不得暴露到非受信网络 |
| 客服模块与 Gateway 策略 | 外仓 | Gateway webflux/webmvc 已支持 MEMBER JWT、`X-MEMBER-ID`、跳过 DPoP/摘要；客服模块仍外仓 |
