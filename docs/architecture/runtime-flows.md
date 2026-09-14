# 核心运行流程

## App 访问会员公开接口

```mermaid
sequenceDiagram
  participant App as 前端 App
  participant IAM as g2rain-iam
  participant Gateway as g2rain 网关
  participant Member as g2rain-member

  App->>IAM: 登录或授权
  IAM-->>App: 签发用于 Gateway 的 Token
  App->>Gateway: 携带 Token 请求会员 API
  Gateway->>Gateway: 校验 Token 与访问上下文
  Gateway->>Member: 转发公开接口请求
  Member-->>App: 经 Gateway 返回会员结果
```

App 和普通外部调用方不得使用企业微信会员解析的受信内部通道。

## 跨模块数据协作

```mermaid
flowchart LR
  Backend[其他后端模块] -->|同步查询| QueryApi[g2rain-member-api 查询契约]
  QueryApi --> Member[g2rain-member]
  App[前端 App] -->|Token| Gateway[g2rain 网关]
  Gateway -->|新增 / 更新业务用例| Member
  Backend -.->|发布领域事实| Event[(领域消息)]
  Event -.->|幂等消费（规划模式）| Member
```

- 其他后端模块同步调用 Member 时以查询为主，不直接使用通用保存、更新或删除接口。
- 交互式编辑由 App 经 Gateway 发起，Member 仍负责最终业务校验和数据变化。
- 非交互式跨模块变化优先由上游发布领域事实，Member 监听后自主更新。
- 虚线消息链路是架构推荐模式，当前源码尚未实现消息消费者；实现时必须补充事件契约、幂等、失败恢复、测试和运维说明。

## 企业微信会员解析或创建

```mermaid
sequenceDiagram
  participant WeCom as 企业微信
  participant Connector as 企业微信智能客服模块
  participant IAM as g2rain-iam
  participant Basis as g2rain-basis
  participant Member as g2rain-member
  participant DB as MySQL

  WeCom->>Connector: 智能客服回调通知
  Connector->>IAM: POST /auth/wecom/customer_service/decrypt
  IAM->>Basis: 校验三方授权 ACTIVE 与企业 Organ 映射
  Basis-->>IAM: organId 等可信事实
  IAM-->>Connector: organId、plainBody、memberResolveCode
  Note over Connector: decrypt 无 external_userid，不创建会员
  Connector->>WeCom: sync_msg 拉取完整消息
  loop 每条消息按 msgid 幂等
    Connector->>IAM: POST /auth/member/token
    IAM->>Member: 受信服务网络内无鉴权直连 resolveOrCreate
    Member->>DB: 查询含逻辑删除的身份 / 必要时同事务创建
    Member-->>IAM: memberId、状态、newMember
    IAM-->>Connector: MEMBER Token + member 摘要（按会话复用）
    opt 需下游业务
      Connector->>Gateway: 持 MEMBER Token 调业务 API
    end
  end
```

详细规则见[企业微信客户接入会员](../design/wechat-work-customer-member-onboarding.md)。

该内部调用不经过 Gateway，也不携带服务凭证。Member 信任 IAM 提供的租户事实，因此 Member 服务必须保持在受信服务网络内，不得向公网、客户端网络或其他非受信网络开放。

## 并发首次创建

```text
查询身份不存在
→ 开启事务
→ 生成 member.id 与 member_no
→ 写入 member
→ 写入 member_identity
→ 若唯一键冲突：回滚当前事务
→ 回查既有身份并按状态返回
```

唯一键冲突不能遗留没有身份的孤立会员；已逻辑删除的身份继续占位，不能自动绑定给其他会员。

## 服务启动

```mermaid
flowchart TD
  A[启动 g2rain-member-startup] --> B[加载 application.yml]
  B --> C[加载 dev 数据源配置]
  B --> D[按需加载 application-nacos.yml]
  C --> E[初始化数据源与 MyBatis]
  D --> F[连接 Nacos 配置与注册中心]
  E --> G[组装会员 Service 与 Controller]
  F --> G
  G --> H[暴露业务接口与 Actuator]
```
