# 构建与部署

## 构建可执行产物

```bash
mvn clean verify
```

可执行 Spring Boot 产物由 `g2rain-member-startup` 模块生成。

## 构建、镜像与 GHCR 发布

本地 Maven 构建与测试使用 `mvn clean verify`；它不会构建容器或推送镜像。根目录脚本先完整 Maven 构建与测试，再使用与 CI 相同的 Startup Jib 配置把镜像写入本机 Docker daemon：

```bash
./build.sh 1.0.0
```

默认标签是 Maven 项目版本；标签必须与项目版本一致，且不使用 `latest`。本地产物为 `ghcr.io/g2rain/g2rain-member:1.0.0`，不会推送 GHCR。

也可手动调用 Maven：

```bash
mvn -pl g2rain-member-startup -am package
mvn -pl g2rain-member-startup jib:dockerBuild
```

手动路径下 POM 默认镜像为 `ghcr.io/g2rain/g2rain-member:<project.version>`。

基础镜像为 `eclipse-temurin:25.0.4.1_1-jre`，主类为 `com.g2rain.member.Application`，容器端口为 `8080`。OCI 元数据包括 source、Apache-2.0 license、title、version 和 description。

GHCR 工作流仅在推送稳定版 `vMAJOR.MINOR.PATCH` Git tag 时运行；普通分支、Pull Request 和手动触发都不会发布镜像。工作流会验证 tag 已进入 `origin/main`、同版本发布说明存在且非空、tag 与 Maven `project.version` 一致，再运行完整 `mvn -U clean install`，使用 `GITHUB_TOKEN` 登录 GHCR 并推送：

- `ghcr.io/g2rain/g2rain-member:<version>`
- `ghcr.io/g2rain/g2rain-member:sha-<完整提交 SHA>`

不创建 GitHub Release、不部署服务，也不更新 `latest`。CI 和本地容器构建复用同一 Jib 的基础镜像、入口、端口、构建产物和 OCI 元数据；CI 的容器步骤会额外推送到 GHCR。匿名拉取仅在包设为公开后可用：

```bash
docker pull ghcr.io/g2rain/g2rain-member:1.0.0
```

首次成功推送后，在 GitHub Packages 的 **Package settings → Change visibility → Public** 将包设为公开；若组织策略禁止修改，请联系组织管理员。若已有同名 GHCR 包推送被拒绝，还须在 **Package settings → Actions access** 授予 `g2rain-member` 写权限，并检查组织 Packages 策略；`packages: write` 本身不能绕过这些设置。

镜像包含 Jib 从 Startup 构建产物组装的依赖、resources 与应用 classes，不是仓库工作区的逐文件 Docker COPY；仓库没有 Dockerfile 或 `.dockerignore`。已跟踪的 `g2rain-member-startup/src/main/resources/application-dev.yml` 会被打入可执行 Jar，进而进入镜像，包含开发数据库用户名和密码。投产必须通过环境变量、Secret 或 Nacos 注入并覆盖凭据，并评估及轮换任何已暴露的默认值；本次未删除文件或改写历史。

## API Maven Packages 发布

镜像双标签推送成功后，GHCR 工作流使用同一 tag 提交调用 `publish-maven.yml`。该工作流也提供手动入口，但所选提交必须位于 `origin/main` 历史。它执行 `mvn -pl g2rain-member-api -am clean deploy`，只发布 `com.g2rain:g2rain-member:1.0.0` 父 POM、`com.g2rain:g2rain-member-api:1.0.0` JAR、POM 与 sources JAR；不发布 Biz 或 Startup。目标仓库为 `https://maven.pkg.github.com/g2rain/g2rain-member`，deploy server ID 为 `github`。Maven 发布失败会让调用 job 失败，已推送的镜像不会自动回滚。重复发布同一稳定版本可能冲突，不删除或覆盖既有版本。

当前外部 G2rain 依赖 `g2rain-common:1.0.9` 与 Starter `1.0.6` 在中央版本登记中指向 Maven Central，因此工作流无需额外 GitHub Maven 依赖仓库 settings。干净 CI 的实际解析与包权限仍需首次运行验证；若日后改用其他仓库的 GitHub Packages，须为每个实际仓库配置认证 repository/server，并授权本仓读取。

消费方需要在 Maven 配置中添加 ID 为 `g2rain-member`、URL 为 `https://maven.pkg.github.com/g2rain/g2rain-member` 的 repository，并在 settings 中配置同 ID server。个人读取使用具备 `read:packages` 的 PAT classic 并安全注入；跨仓库 Actions 的 `GITHUB_TOKEN` 读取须核对 Package settings → Actions access 与组织 Packages 策略。Public GHCR 匿名拉取不代表 Maven Packages 可匿名下载。

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
