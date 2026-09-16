# 架构例外

本项目采用 g2rain `java-domain-service 1.0.0`（`architecture-v1.0.0`）。这里只记录相对中央基线的有意偏离；符合 Profile 的领域差异不作为例外。

## DEV-001：企业微信会员解析同步写入

### 状态

已接受，随 `java-domain-service 1.0.0` 完成复审。

### 基线规则

其他后端模块同步依赖默认以查询为主，数据变化优先通过领域消息通知数据所有者。

### 当前例外

`g2rain-iam` 在客服链路中编排会员解析：校验 `memberResolveCode` 后，通过服务发现无鉴权直连 Member 的 `resolveOrCreate` 用例。该用例可能在同一事务中创建 `member` 和 `member_identity`，属于跨模块同步写入例外。

企业微信智能客服模块位于 Gateway 外侧，**不**直连 Member；只调用 IAM 的 `decrypt` / `token` 接口。

### 原因

- 首次外部联系人识别需要立即得到稳定 `memberId` 以关联咨询上下文。
- 客服模块为应用层组件，不得直连 Member；由 IAM 在受信服务网络内直连 `/internal/...`，避免把 Member 内部接口暴露给应用层。
- Member 必须集中执行身份唯一性、历史身份占位、租户一致性和并发冲突处理。
- 当前项目尚未实现跨模块领域消息消费者。

### 约束

- 逻辑调用方必须是 `g2rain-iam`；Member 不鉴别调用方身份，信任 IAM 传入的 `organId`，因此该保证由部署拓扑和网络边界承担。
- Member 服务及 `/internal/wechat_work_member/**` 不得暴露到公网、客户端网络或其他非受信网络；生产网络策略应只允许约定的受信服务访问。
- `decrypt`（`POST /auth/wecom/customer_service/decrypt`）阶段无 `external_userid`，不得创建会员；须在 `sync_msg` 之后由 IAM `POST /auth/member/token` 编排。
- 接口只接受 `organId`、`externalUserId` 和白名单资料，不接收回调密文、拉取令牌或完整消息。
- 普通 App、客服模块和其他后端不得直接访问该 Member 内部接口。
- IAM 在 `token` 成功后可签发 `SessionType=MEMBER` 短期 Token（按 `organId + external_userid + applicationCode` 会话复用），供**仅**客服模块经 Gateway 调下游业务；不创建 `passport`，不下发终端用户。
- `member` 与 `member_identity` 必须同事务提交或回滚；并发冲突不能遗留孤立会员。
- 接口必须保持幂等，并覆盖跨租户、已删除身份、并发和事务回滚测试。

### 已接受的信任模型

- `IAM → Member` 使用服务发现直连，不经过 Gateway，不携带服务凭证，Member 不做调用方鉴权。
- IAM 是 `organId`、`externalUserId` 和白名单资料的可信编排方；Member 仍负责租户一致性、身份唯一性、状态和事务规则。
- 该模式以受信服务网络为安全边界。若 Member 需要进入共享、不受信或公网可达网络，必须先引入调用方鉴权再调整部署。

### 复审条件

- 引入可靠的会员识别事件和请求/结果关联机制。
- 企业微信接入链路允许最终一致性，且不再要求同步返回 `memberId`。
- 中央 Profile 改变同步写入例外规则。

## 符合基线但需要说明的项目差异

- `MemberDto`、`MemberIdentityDto` 位于 Biz，供 App 经 Gateway 使用的本地写入用例，不作为后端模块间契约；这符合 DTO Policy，不是例外。
- `member_identity` 的唯一索引不包含 `delete_flag`，用于让历史身份永久占位；这符合中央数据库规范中的“永久占位”语义，不是例外。
- 当前没有领域消息消费者。文档中的消息模式是推荐演进方向，不是现有能力。
