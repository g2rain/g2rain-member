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
| `service` | 会员 CRUD、身份 CRUD 与企业微信会员解析。 |
| `dao` / `dao.po` | MyBatis 数据访问和持久化对象。 |
| `converter` | DTO、VO 与 PO 转换。 |
| `domain` | 会员编号生成和外部资料清理等纯领域能力。 |

约束：

- 依赖 API 实现契约，不得依赖 Startup。
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

