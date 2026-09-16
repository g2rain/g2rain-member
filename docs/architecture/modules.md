# 模块职责

## `g2rain-member-api`

会员领域契约模块，供当前服务实现和受信平台服务共同依赖。

| 目录 | 职责 |
| --- | --- |
| `api` | 会员、会员身份及企业微信内部解析接口。 |
| `dto` | 查询条件、内部请求和外部资料白名单对象。 |
| `vo` | 会员、身份及解析结果。 |
| `enums` | 错误码、会员状态和身份类型。 |

约束：

- 不得依赖 `g2rain-member-biz` 或 `g2rain-member-startup`。
- 不得引用 Controller、Service、DAO、Config、Converter 或持久化对象。
- 内部契约只接收经过上游验证的 `organId`、身份值和最小化资料。

## `g2rain-member-biz`

会员领域实现模块。

| 目录 | 职责 |
| --- | --- |
| `controller` | HTTP 接口适配、参数校验和受信入口。 |
| `dto` | Member 本地写入模型；当前包含 `MemberDto`、`MemberIdentityDto`，用于 App 经 Gateway 发起的新增/更新请求、Service 入参及到 PO 的转换。 |
| `service` | 会员 CRUD、身份 CRUD 与企业微信会员解析。 |
| `dao` / `dao.po` | MyBatis 数据访问和持久化对象。 |
| `converter` | DTO、VO 与 PO 转换。 |
| `domain` | 会员编号生成和外部资料清理等纯领域能力。 |

约束：

- 依赖 API 实现契约，不得依赖 Startup。
- Biz `dto` 不等同于 API 模块中的同名包：其类型属于实现模块，只能在当前服务的 Controller、Service、Converter 等 Biz 代码中使用，不能作为其他服务依赖的契约。
- `MemberDto` 和 `MemberIdentityDto` 虽然由 HTTP `/save` 接口接收，但当前未被 `g2rain-member-api` 的接口声明引用，因此其架构角色是“实现本地的新增/更新输入”，不是查询条件，也不是跨服务 API DTO。
- 这种分布体现“模块间同步依赖以查询为主”的设计：App 可以经 Gateway 调用 Member 写接口，但其他后端模块不应通过 API JAR 获得通用保存/更新契约。
- 少数确需跨模块同步写入的能力必须使用具有明确业务语义的命令契约，经过需求设计和 ADR 评审后定义到 API 模块；不能让 API 接口反向引用 Biz DTO。
- 其他模块引起的数据变化优先发布领域消息，由 Member 消费后在自身事务和规则内完成编辑。
- 会员与身份的联合创建、租户一致性、逻辑删除和并发冲突处理必须位于 Service/领域层。
- `WithoutIsolation` 数据访问只能用于已有显式租户校验的受信流程。

## `g2rain-member-startup`

运行时组装模块：

- 提供 `com.g2rain.member.Application` 启动类。
- 引入 Biz 并组装 Spring Web、Actuator 和 OpenAPI。
- 提供端口、profile、数据源、Nacos、MyBatis 与观测配置。
- 通过 Jib 构建 `g2rain/g2rain-member` 镜像。

该模块不承载会员业务规则，也不新增供下层模块反向依赖的领域类型。

## 根工程

根 `pom.xml` 统一维护 Java、Spring Boot、Spring Cloud、内部组件版本、插件和三个 Maven 模块。`codegen.properties` 仅用于代码生成，修改时应遵循[代码生成说明](../development/code-generation.md)。

## DTO 所属关系

API 和 Biz 模块当前都包含物理包名 `com.g2rain.member.dto`。判断 DTO 的架构归属时必须查看源码所在的 Maven 模块，不能只根据 Java 包名判断：

| 源码位置 | 当前类型 | 架构角色 | 可见性边界 |
| --- | --- | --- | --- |
| `g2rain-member-api/src/main/java/com/g2rain/member/dto` | `MemberSelectDto`、`MemberIdentitySelectDto` | 会员和会员身份查询契约 | 可由 API 使用方复用 |
| 同上 | `WechatWorkMemberResolveRequest`、`WechatWorkExternalProfileDto` | 受信企业微信会员解析契约与白名单资料 | **仅** `g2rain-iam` 经 Gateway 复用；客服模块不得直连 |
| `g2rain-member-biz/src/main/java/com/g2rain/member/dto` | `MemberDto`、`MemberIdentityDto` | App 经 Gateway 使用的 Member 写入输入及 Biz 内部传输模型 | 不作为后端模块间同步契约 |

Biz DTO 继承 `BaseDto`，携带创建/更新校验和可写字段，经 `MemberConverter` 或 `MemberIdentityConverter` 转为 PO。它们不应直接用作查询条件、返回对象或其他服务的依赖类型。
