# 依赖治理规范

## 引入原则

- 优先使用 JDK、Spring Boot、现有 g2rain Starter 和仓库已有依赖解决问题。
- 新依赖必须对应明确需求，不能仅为替代少量简单代码而引入大型库。
- 评估维护状态、许可证、安全漏洞、Java 25/Spring Boot 4 兼容性和传递依赖。
- API 模块保持轻量，不引入数据库、运行时实现或 Biz/Startup 依赖。

## 版本管理

- 公共版本统一放在根 POM `properties` 或 `dependencyManagement`，子模块不重复散落版本。
- 优先遵循 Spring Boot、Spring Cloud BOM；覆盖 BOM 版本时记录原因和兼容性依据。
- g2rain 内部 JAR 升级应核对 API 兼容性、调用方和发布顺序。
- `main` 发布 `common`、`starter` 等 JAR 项目时必须按兼容性升级版本号。

## 模块边界

- API 不依赖 Biz 或 Startup。
- Biz 可以依赖 API、持久化和必要平台 Starter，不依赖 Startup。
- Startup 负责运行时依赖和最终组装，下层模块不得反向依赖。
- 跨项目调用优先依赖对方 API 契约，不依赖实现模块。

## 变更要求

新增或升级依赖的 PR 应说明：

- 使用场景和没有复用现有能力的原因。
- 直接及关键传递依赖变化。
- 许可证、安全和运行时兼容性。
- 测试结果、部署影响和回退方式。

依赖变化后执行 `mvn clean verify`，并检查有效 POM、依赖树和最终镜像是否出现非预期组件。

