# 本地开发

## 环境准备

- JDK 25
- Maven 3.9+
- MySQL 8+
- Redis（使用相关平台 Starter 能力时）
- Nacos（使用 `nacos` profile 时）

建议使用独立本地数据库、Nacos namespace 和测试凭证，不要复用生产环境配置。

## 初始化数据库

`scripts/g2rain-member.sql` 会删除并重建会员表，只能用于首次初始化或可丢弃的开发库：

```bash
mysql -u root -p < scripts/g2rain-member.sql
```

## 构建与测试

```bash
mvn clean verify
```

只验证业务模块及其依赖：

```bash
mvn -pl g2rain-member-biz -am test
```

## 启动

```bash
mvn -pl g2rain-member-startup -am spring-boot:run
```

默认端口 `8080`，默认 profile `dev`：

```powershell
$env:SERVER_PORT = '8080'
$env:SPRING_PROFILES_ACTIVE = 'dev'
mvn -pl g2rain-member-startup -am spring-boot:run
```

需要 Nacos 时显式启用组合 profile：

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev,nacos'
$env:NACOS_SERVER_ADDR = '127.0.0.1:8848'
mvn -pl g2rain-member-startup -am spring-boot:run
```

## 调试入口

- 健康检查：`GET /actuator/health`
- OpenAPI：由 `g2rain-starter-spring-doc` 提供，实际地址受平台配置影响。
- Mapper：`g2rain-member-biz/src/main/resources/mybatis/mapper`。
- 服务名：`g2rain-member`。

提交前运行 `mvn clean verify`。若修改模块、领域边界、配置、生成流程或长期设计，应同步更新文档或 ADR。

