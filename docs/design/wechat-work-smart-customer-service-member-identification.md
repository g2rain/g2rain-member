# 企业微信智能客服会员识别流程

## 1. 文档目的

本文定义企业微信会员通过智能客服发起咨询时的会员识别、身份可信度、模块职责和异常处理规则，作为后续接口与业务代码实现依据。

本流程只识别当前租户内的会员，不创建 `passport`，不为会员建立 `g2rain-iam` 登录会话。

## 2. 相关定义

- [`member` 会员表](../../scripts/g2rain-member.sql#L21-L40)：租户 `organ` 内的会员主体。
- [`member_identity` 会员身份表](../../scripts/g2rain-member.sql#L46-L65)：保存企业微信、手机号等会员身份入口。
- [`organ` 机构表](../../../g2rain-basis/scripts/g2rain-basis.sql#L62)：租户主体，由企业微信接入配置最终映射到 `organ_id`。
- [会员编号生成规范](./member-no-generation.md)：首次创建会员时生成 `member_no`。
- [完整会员数据库脚本](../../scripts/g2rain-member.sql)：当前会员系统第一版表结构。
- [IAM 企业微信客服回调认证升级设计](../../../../../github/g2rain-iam/docs/design/wecom-customer-service-callback-verification.md)：企业微信回调验签、解密与可信租户上下文设计。

数据库和两张会员表统一使用 MySQL 8.0.13+ `utf8mb4_0900_as_cs` 排序规则。会员编号、身份类型、企业微信 `external_userid` 等字符串的比较均大小写敏感；调用方不得依赖数据库进行大小写归一化。

## 3. 核心结论

企业微信会员不会登录 `g2rain-iam`，但企业微信渠道的真实性由 `g2rain-iam` 验证：

```text
企业微信客服回调
    ↓
g2rain-iam 验签、解密并给出可信租户上下文
    ↓
企业微信接入模块通过 sync_msg 拉取完整消息
    ↓
organ_id + external_userid
    ↓
member_identity
    ↓
member
```

`g2rain-iam` 验证的是企业微信回调渠道，而不是为外部联系人完成平台登录。企业微信外部联系人不转换为 `passport`，也不创建 IAM Session。

## 4. 模块职责

### 4.1 企业微信接入模块

- 接收企业微信智能客服回调。
- 将签名、时间戳、随机串和密文提交给 `g2rain-iam` 验证、解密。
- 从解密后的 `kf_msg_or_event` 通知中读取拉取令牌和 `OpenKfId`。
- 调用企业微信 `/cgi-bin/kf/sync_msg` 拉取完整消息。
- 从拉取结果中提取 `msgid`、`open_kfid`、`external_userid` 和消息内容。
- 使用 IAM 返回的可信租户上下文确定 `organ_id`。
- 按企业微信 `msgid` 对完整消息做幂等处理，避免重复消费。

智能客服回调本身只是“有新消息或事件”的通知，不直接包含可供会员识别的完整消息内容。只有 IAM 验签解密成功，并通过企业微信可信接口拉取到消息后，才能使用其中的 `external_userid`。禁止使用普通请求参数直接传入的 `organ_id` 或 `external_userid` 识别会员。

### 4.2 g2rain-member

- 按 `organ_id + WECHAT_WORK + external_userid` 查询会员身份。
- 首次咨询时，在同一事务中创建 `member` 和 `member_identity`。
- 返回稳定的 `member_id`，供咨询、工单和后续业务关联。
- 检查会员冻结、删除等状态。
- 负责后续手机号验证码绑定及身份可信级别提升。

### 4.3 g2rain-iam

- 管理企业微信回调密钥，并按回调场景选择正确的 `Token`、`EncodingAESKey` 和期望接收方。
- 校验企业微信回调签名、时间戳与随机串，解密回调密文。
- 返回已验证的企业标识和可信 `organ_id` 上下文。
- 认证租户员工、人工客服和后台管理人员。
- 为内部应用或服务间调用签发、校验访问令牌。
- 控制谁可以查看会员资料、咨询内容和敏感业务数据。
- 不调用微信客服 `sync_msg`，不处理客服消息内容。
- 不根据 `external_userid` 查询会员。
- 不为企业微信会员创建 `passport`。
- 不为每条客服消息或每个企业微信会员创建登录 Session。

### 4.4 智能客服业务模块

- 使用 `member_id` 关联对话、咨询、工单和业务上下文。
- 根据会员状态和身份可信级别决定可提供的服务范围。
- 对查询订单、退款、修改资料等敏感操作触发增强验证。

## 5. 主业务流程

```text
企业微信会员发送咨询消息
        ↓
企业微信推送智能客服回调
        ↓
企业微信接入模块调用 IAM
校验签名、时间戳、随机串并解密
        ↓
IAM 返回可信 organ_id、企业标识和回调明文
        ↓
解析 kf_msg_or_event、拉取令牌、OpenKfId
        ↓
调用 /cgi-bin/kf/sync_msg 拉取完整消息
        ↓
逐条读取 msgid、external_userid 和消息内容
        ↓
按 msgid 幂等消费
        ↓
查询 member_identity：
organ_id = 当前租户
identity_type = WECHAT_WORK
identity_value = external_userid
delete_flag = 0
        ↓
    ┌── 已存在 ──→ 查询 member 并检查状态
    │
    └── 不存在 ──→ 事务内创建 member
                    + 创建 WECHAT_WORK identity
                    + verified = 1
        ↓
获得 member_id
        ↓
智能客服处理咨询并记录业务关联
        ↓
普通咨询直接响应；敏感操作要求增强验证
```

一次回调可能触发拉取多条消息，每条消息应分别按 `msgid` 幂等处理。`sync_msg` 的游标、拉取令牌和重试状态属于企业微信接入模块，不写入会员表。

企业微信身份的 `verified = 1` 只表示该身份来自 IAM 已验证的企业微信渠道，并由企业微信可信接口返回，不表示手机号、真实姓名或法定实名已经验证。

## 6. 首次会员创建规则

首次通过 `sync_msg` 拉取到可信企业微信会员消息且身份不存在时：

1. 使用项目统一 ID 生成器生成 `member.id`。
2. 按[会员编号生成规范](./member-no-generation.md)生成 `member.member_no`。
3. 设置 `member.organ_id` 为接入配置确定的租户。
4. 可将企业微信昵称、头像同步到 `member.name`、`member.avatar`；缺失时允许为空。
5. 创建 `member_identity`：
   - `organ_id`：与 `member.organ_id` 一致。
   - `member_id`：新会员 ID。
   - `identity_type`：`WECHAT_WORK`。
   - `identity_value`：企业微信 `external_userid`。
   - `verified`：`1`。
   - `raw_profile`：保存必要的外部资料快照，不保存令牌、密钥等敏感凭据。
6. `member` 与 `member_identity` 必须在同一数据库事务中提交。

并发首次消息可能同时触发创建。实现应依赖 [`uk_organ_identity`](../../scripts/g2rain-member.sql#L59) 阻止重复身份；发生唯一键冲突时必须在事务回滚后回查包含逻辑删除记录的身份：有效身份继续处理，已删除身份按永久占位规则拒绝自动创建。

## 7. 身份可信级别与业务权限

| 身份状态 | 能证明的内容 | 建议允许的业务 |
|---|---|---|
| 企业微信身份已验证 | 消息来自当前租户下的该企业微信外部联系人 | 普通咨询、公开信息、非敏感权益说明 |
| 手机号已验证 | 当前会员控制已绑定手机号 | 查询与手机号关联的个人业务、低风险资料操作 |
| 独立实名认证完成 | 会员通过指定实名流程 | 退款、关键资料修改等高风险操作，具体仍由业务规则决定 |

手机号验证成功后，向同一个 `member_id` 增加 `identity_type = MOBILE` 的身份，不新建会员。

手机号数据规则：

- `member_identity(identity_type = MOBILE).identity_value` 是手机号身份的权威数据。
- `member.mobile` 是用于列表和资料展示的冗余字段，不参与登录身份判断。
- 首次绑定手机号时，在同一事务中创建 `MOBILE` 身份并同步 `member.mobile`。
- 换绑手机号时，必须先验证新手机号，再在同一事务中更新已有 `MOBILE` 身份和 `member.mobile`，不创建第二条 `MOBILE` 身份。
- 解绑手机号时，逻辑删除 `MOBILE` 身份并清空 `member.mobile`。
- 重新绑定时优先恢复并更新该会员原有的 `MOBILE` 身份记录，不新增同类型记录。
- 如果新手机号已绑定或曾绑定到同一租户的其他会员，禁止自动迁移，必须先完成会员合并或人工核验。

## 8. 会员状态处理

- `NORMAL`：正常进入咨询流程。
- `FROZEN`：允许返回必要的通用说明；会员资料查询、权益领取和敏感操作应被拒绝或转人工处理。
- `member.delete_flag = 1`：不作为有效会员返回，第一版禁止通过外部身份自动恢复或创建替代会员。
- `member_identity.delete_flag = 1`：该身份作为历史归属保留，不得通过普通首次入会流程绑定给其他会员。
- 企业微信身份已删除后再次出现：返回 `MEMBER_IDENTITY_DELETED`，转人工恢复或会员合并流程，不自动新建会员。
- 手机身份重新绑定：只允许在验证通过后恢复当前会员自己的原身份记录；不得恢复其他会员的历史手机号身份。

身份唯一索引不包含 `delete_flag` 是有意设计：已删除身份继续占用其租户内唯一值，防止外部身份在没有核验的情况下被自动转移到另一个会员。它属于“永久占位”语义，因此不使用仅有效记录唯一的 `IF(delete_flag = 0, 0, NULL)` 函数索引。

## 9. 企业微信租户路由前置项

当前会员数据库只定义了 `member` 和 `member_identity`，尚未定义企业微信接入配置或企业标识到 `organ_id` 的映射表。实现本流程前必须由企业微信接入模块提供可信的租户路由能力。

路由至少需要保证：

- 一个回调能唯一确定 `organ_id`。
- 企业标识、客服账号与 `organ_id` 的映射由平台配置产生，不能由会员请求指定。
- IAM 验签使用的回调密钥、期望接收方与对应接入配置一致。
- 配置停用后拒绝继续识别会员。

如果后续需要新增接入配置表，应放在企业微信集成或租户配置边界内，不放入 `member_identity`；`member_identity` 只保存会员与外部身份的绑定关系。

## 10. 建议的领域接口

会员模块可提供以下语义接口，具体类名和传输协议在实现阶段确定：

```text
resolveOrCreateWechatWorkMember(
    organId,
    externalUserId,
    externalProfile
) -> MemberIdentityResult
```

返回结果至少包含：

- `memberId`
- `memberNo`
- `memberStatus`
- `newMember`
- `identityVerified`

接口必须由可信内部调用方访问；如果通过服务网络暴露，应使用 `g2rain-iam` 或平台既有机制验证调用应用，但这属于服务调用认证，不代表对会员本人进行 IAM 登录认证。

### 10.1 member 模块建议分层

后续代码实现至少包含以下职责，具体包名遵循项目代码生成结果：

```text
API 层
  ├── 企业微信会员解析请求 DTO
  ├── 会员身份解析结果 VO
  └── 内部调用 API

Biz 层
  ├── Member / MemberIdentity 领域实体
  ├── MemberService
  ├── MemberIdentityService
  ├── WechatWorkMemberResolver
  └── DAO / Repository
```

`WechatWorkMemberResolver` 只接收上游已经验证的 `organId`、`externalUserId` 和必要的脱敏资料，不接收企业微信回调密文、签名或拉取令牌。

### 10.2 查询与创建算法

```text
标准化并校验 organId、externalUserId
        ↓
按 organ_id + WECHAT_WORK + external_userid 查询身份（包含逻辑删除记录）
        ↓
存在且有效：查询 member，校验 organ_id、delete_flag、status 后返回
        ↓
存在但已删除：返回 MEMBER_IDENTITY_DELETED，不创建新会员
        ↓
不存在：开启事务
        ↓
生成 member.id 与 member_no
        ↓
写入 member
        ↓
写入 member_identity(verified=1)
        ↓
提交并返回
```

写入身份时如果触发 `uk_organ_identity` 唯一键冲突，说明并发请求已经完成创建。当前事务应回滚新建会员，再查询已有有效身份并返回，避免遗留没有身份的孤立 `member`。

### 10.3 输入校验

- `organId` 必须为正数，并且来自可信内部调用上下文。
- `externalUserId` 去除首尾空白后不能为空，保存时保持企业微信返回值原样，不自行改变大小写；数据库使用大小写敏感比较。
- `externalProfile` 只能包含允许落库的昵称、头像等字段，禁止包含回调密钥、访问令牌、拉取令牌和完整消息内容。
- 查询到的 `member_identity.organ_id`、`member.organ_id` 必须与请求租户一致。
- 返回会员前必须同时检查身份和会员的 `delete_flag`。

### 10.4 建议业务错误

| 场景 | 建议错误语义 |
|---|---|
| 租户标识无效 | `MEMBER_ORGAN_INVALID` |
| 企业微信外部联系人标识缺失 | `MEMBER_WECHAT_WORK_IDENTITY_INVALID` |
| 身份指向的会员不存在 | `MEMBER_IDENTITY_BROKEN` |
| 身份与会员租户不一致 | `MEMBER_IDENTITY_ORGAN_MISMATCH` |
| 企业微信身份已逻辑删除 | `MEMBER_IDENTITY_DELETED` |
| 会员已冻结 | `MEMBER_FROZEN` |
| 会员或身份已删除 | `MEMBER_NOT_FOUND` |
| 并发创建后仍无法回查身份 | `MEMBER_IDENTITY_CREATE_CONFLICT` |

错误码的最终编号和枚举位置在实现阶段按项目统一规范确定。

## 11. 异常与安全处理

- IAM 回调验证失败：企业微信接入模块拒绝处理，不调用会员解析接口。
- 无法确定租户：拒绝处理并告警，不允许使用默认 `organ_id`。
- `sync_msg` 拉取失败：按企业微信协议重试，不提前创建会员。
- 拉取结果缺少 `external_userid`：不执行会员识别，按消息或事件类型分别处理。
- 重复 `msgid`：返回幂等结果，不重复解析会员或执行咨询业务。
- 身份唯一键冲突：回查有效身份；如果指向异常会员或跨租户数据不一致，停止处理并告警。
- 查询到已逻辑删除的企业微信身份：不尝试新增，返回 `MEMBER_IDENTITY_DELETED` 并转人工流程。
- 会员冻结或删除：按状态规则限制业务，不绕过状态校验。
- 日志不得完整输出回调密钥、访问令牌、手机号验证码或不必要的个人资料。
- `raw_profile` 只保存后续识别或展示确有需要的字段，并遵循数据最小化原则。

## 12. 实现清单

- [ ] 与 [IAM 升级设计](../../../../../github/g2rain-iam/docs/design/wecom-customer-service-callback-verification.md)确认可信 `organ_id` 的传递契约。
- [ ] 定义企业微信会员解析 API、DTO、VO 和错误码。
- [ ] 根据 `member`、`member_identity` 表生成或编写实体与数据访问层。
- [ ] 实现 `WECHAT_WORK` 身份查询。
- [ ] 实现首次会员与身份的事务创建。
- [ ] 实现唯一键冲突时事务回滚及已有身份回查。
- [ ] 实现会员状态校验。
- [ ] 实现已删除身份的永久占位、人工恢复和禁止自动转移规则。
- [ ] 实现手机号首次绑定、换绑、解绑及 `member.mobile` 同事务同步。
- [ ] 保证身份、会员和请求 `organ_id` 一致。
- [ ] 限制 `raw_profile` 白名单字段并完成日志脱敏。
- [ ] 为敏感操作定义手机号或实名增强验证流程。
- [ ] 添加已有身份、首次创建、并发创建、跨租户隔离、冻结会员、逻辑删除和损坏绑定测试。
