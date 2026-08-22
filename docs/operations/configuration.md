# 配置说明

主配置入口为 `g2rain-member-startup/src/main/resources/application.yml`；本地数据源位于 `application-dev.yml`，Nacos 配置位于 `application-nacos.yml`。

## 基础配置

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP 服务端口。 |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring profile；接入 Nacos 时使用 `dev,nacos` 等组合。 |
| `NACOS_SERVER_ADDR` | `127.0.0.1:8848` | Nacos 服务地址。 |
| `SPRING_CLOUD_NACOS_DISCOVERY_NAMESPACE` | `dev` | 注册中心 namespace。 |
| `SPRING_CLOUD_NACOS_CONFIG_NAMESPACE` | `dev` | 配置中心 namespace。 |
| `g2rain.data.isolation.enabled` | `true` | 是否启用租户数据隔离。 |

Nacos discovery group 固定为 `g2rain`，服务名和配置 group 为 `g2rain-member`。

## 数据源

开发 profile 默认连接本机 MySQL 的 `g2rain_member` 数据库。数据库地址、账号和密码应在共享或生产环境中由环境、Secret 或 Nacos 覆盖，不应继续使用开发默认值。

MyBatis Mapper 扫描路径为：

```text
classpath:/mybatis/mapper/*.xml
```

## 上传与观测

- 单文件默认上限：10 MB。
- 单请求默认上限：20 MB。
- Actuator 暴露 `health`、`info` 和 `metrics`；生产环境必须限制访问范围。
- DAO 调试日志 `com.g2rain.member.dao: debug` 适用于开发环境，生产环境应降低级别并避免敏感数据进入日志。

## 敏感信息

- 不得提交生产数据库、Nacos、Redis、企业微信或其他外部系统凭证。
- `codegen.properties` 和 `application-dev.yml` 的默认凭证只适用于隔离本地环境。
- `raw_profile`、日志和接口响应遵循数据最小化原则。

