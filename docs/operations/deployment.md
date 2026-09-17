# 构建与部署

## 构建可执行产物

```bash
mvn clean verify
```

可执行 Spring Boot 产物由 `g2rain-member-startup` 模块生成。

## 构建镜像

推荐使用仓库根目录脚本（与 department 一致）：先全量 `install`，再对 startup 执行 Jib：

```bash
./build.sh <tag>
```

不传 tag 时默认为 `latest`，产物为 `g2rain/g2rain-member:<tag>`。

也可手动调用 Maven：

```bash
mvn -pl g2rain-member-startup -am package
mvn -pl g2rain-member-startup jib:dockerBuild
```

手动路径下 POM 默认镜像为 `g2rain/g2rain-member:<project.version>`。

基础镜像为 `eclipse-temurin:25-jre`，主类为 `com.g2rain.member.Application`，容器端口为 `8080`。

## 部署前检查

- MySQL 版本不低于 `8.0.13`，并与本地、CI 和测试环境保持兼容。
- MySQL 结构已安全升级；不要在已有数据环境重复执行会删表的初始化 SQL。
- Nacos namespace、group、服务名与凭证正确。
- Redis 和数据库已按环境隔离。
- 所有敏感配置由部署环境注入。
- Gateway 已配置公开会员 API 与 `SessionType=MEMBER` Token 鉴权策略；IAM→Member 的内部解析不经过 Gateway。
- Member 仅部署在受信服务网络，未暴露到公网、客户端网络或其他非受信网络；网络策略仅开放必要的服务间访问。
- 企业微信链路：IAM `decrypt`（`memberResolveCode`）→ 客服 sync_msg → IAM `POST /auth/member/token` → Member；客服模块不直连 Member。
- `mvn clean verify` 已通过，Agent 已核对文档、POM、源码与 Git Diff。

## 运行观测

Actuator 暴露 `health`、`info` 和 `metrics`。生产环境应通过网络策略、网关或管理端口限制访问，不要直接暴露管理接口。
