# 架构总览

## 系统职责

`g2rain-member` 位于 g2rain 平台会员领域层，是租户内会员主数据和外部会员身份关系的权威服务。

它解决四类问题：

1. 为租户内会员维护稳定、无业务含义的会员编号和生命周期状态。
2. 将企业微信 `external_userid`、手机号等身份绑定到同一个会员主体。
3. 在可信渠道首次识别会员时，事务性、幂等地创建会员与身份。
4. 通过租户隔离、历史身份占位和资料最小化降低串租户及身份误绑定风险。

## 系统上下文

```mermaid
flowchart LR
  App[前端 App] -->|登录 / 授权| IAM[g2rain-iam]
  IAM -->|签发用于 Gateway 的 Token| App
  App -->|携带 Token| Gateway[g2rain 网关]
  Gateway -->|会员公开 API| Member[g2rain-member]
  WeCom[企业微信] -->|回调通知| Connector[企业微信接入模块]
  Connector -->|验签、解密、可信租户确认| IAM
  Connector -->|受信内部会员解析| Member
  Member --> MySQL[(MySQL)]
  Member --> Redis[(Redis)]
  Member --> Nacos[Nacos]
  Member -. organ_id 语义关联 .-> Basis[g2rain-basis]
```

- 前端 App 从 IAM 获得 Token，再通过 Gateway 访问 Member 的公开接口。
- 企业微信接入模块先让 IAM 验证回调并取得可信 `organ_id`，拉取完整消息后才能调用 Member 的受信内部接口。
- Member 不接收企业微信回调密文、签名、拉取令牌或完整消息，也不为外部联系人创建 Passport 或 IAM Session。
- `member.organ_id` 与 Basis 的组织主数据存在语义关联；当前代码不因此声明对 `g2rain-basis-api` 的直接依赖。

## 容器与模块

```text
g2rain-member-startup
        │ depends on
        ▼
g2rain-member-biz
        │ depends on
        ▼
g2rain-member-api
```

API 模块发布稳定契约；Biz 模块实现会员规则、Web 适配和持久化；Startup 模块负责运行时组装。

## 核心领域

| 领域 | 代表对象 | 主要职责 |
| --- | --- | --- |
| 会员主体 | `Member` | 维护租户、会员编号、资料、状态和删除标识。 |
| 会员身份 | `MemberIdentity` | 维护会员与企业微信、手机号等身份的唯一绑定。 |
| 会员编号 | `MemberNoGenerator` | 根据全局 ID 生成稳定且无业务含义的展示编号。 |
| 企业微信识别 | `WechatWorkMemberResolver` | 根据可信租户和外部联系人标识解析或幂等创建会员。 |
| 资料最小化 | `WechatWorkExternalProfileSanitizer` | 仅保留允许落库的昵称、头像等必要资料。 |

## 非职责

- 不认证平台员工或会员，不签发 Token、授权码或 Session。
- 不负责企业微信回调验签、解密、消息拉取和消息幂等消费。
- 不承载咨询、工单、订单、权益等下游业务。
- 不实现网关路由、统一入口鉴权或管理端页面。

