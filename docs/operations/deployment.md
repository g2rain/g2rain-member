# 构建与部署

## 构建可执行产物

```bash
mvn clean verify
```

可执行 Spring Boot 产物由 `g2rain-member-startup` 模块生成。

## 构建镜像

Startup 模块配置了 Jib。先构建依赖模块，再构建到本地 Docker：

```bash
mvn -pl g2rain-member-startup -am package
mvn -pl g2rain-member-startup jib:dockerBuild
```

目标镜像默认为：

```text
g2rain/g2rain-member:<project.version>
```

基础镜像为 `eclipse-temurin:25-jre`，主类为 `com.g2rain.member.Application`，容器端口为 `8080`。

## 部署前检查

- MySQL 版本不低于 `8.0.13`，并与本地、CI 和测试环境保持兼容。
- MySQL 结构已安全升级；不要在已有数据环境重复执行会删表的初始化 SQL。
- Nacos namespace、group、服务名与凭证正确。
- Redis 和数据库已按环境隔离。
- 所有敏感配置由部署环境注入。
- Gateway 已配置公开会员 API 的路由和鉴权。
- 企业微信受信调用已完成 IAM 回调验证、可信租户传递和内部服务访问控制。
- `mvn clean verify` 已通过，Agent 已核对文档、POM、源码与 Git Diff。

## 运行观测

Actuator 暴露 `health`、`info` 和 `metrics`。生产环境应通过网络策略、网关或管理端口限制访问，不要直接暴露管理接口。
