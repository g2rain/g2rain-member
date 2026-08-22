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

## 企业微信会员解析或创建

```mermaid
sequenceDiagram
  participant WeCom as 企业微信
  participant Connector as 企业微信接入模块
  participant IAM as g2rain-iam
  participant Member as g2rain-member
  participant DB as MySQL

  WeCom->>Connector: 智能客服回调通知
  Connector->>IAM: 验签、解密并确认租户
  IAM-->>Connector: 可信 organId 与回调上下文
  Connector->>WeCom: sync_msg 拉取完整消息
  Connector->>Member: organId + externalUserId + 白名单资料
  Member->>DB: 查询包含逻辑删除的身份
  alt 身份有效
    Member->>DB: 查询并校验会员
  else 身份不存在
    Member->>DB: 同事务创建会员与身份
  else 身份已删除或数据异常
    Member-->>Connector: 返回明确业务错误
  end
  Member-->>Connector: memberId、memberNo、状态与创建标识
```

详细规则见[企业微信智能客服会员识别](../design/wechat-work-smart-customer-service-member-identification.md)。

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

