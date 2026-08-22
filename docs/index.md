# g2rain-member 文档

这里是 `g2rain-member` 的项目级技术文档，也是根 README、架构验证和后续文档站点的事实来源。

## 项目定位

`g2rain-member` 是 g2rain 平台的会员领域服务，维护租户内会员、稳定会员编号及外部身份绑定，并为企业微信等可信接入渠道提供会员解析与幂等创建能力。

## 阅读路径

### 了解架构

- [架构总览](architecture/overview.md)
- [模块职责](architecture/modules.md)
- [依赖边界](architecture/dependencies.md)
- [核心运行流程](architecture/runtime-flows.md)

### 开发与验证

- [本地开发](development/local-development.md)
- [代码规范](development/code-conventions.md)
- [Git 分支与提交策略](development/git-workflow.md)
- [CRUD 代码生成](development/code-generation.md)
- [架构决策记录](decisions/README.md)

### 运行与维护

- [配置说明](operations/configuration.md)
- [构建与部署](operations/deployment.md)
- [故障排查](operations/troubleshooting.md)

### 专题设计

- [会员编号生成规范](design/member-no-generation.md)
- [企业微信智能客服会员识别](design/wechat-work-smart-customer-service-member-identification.md)

### 社区

- [社区、贡献、联系方式与许可证](community.md)

## 文档维护约定

- README 只保留项目定位、快速开始、架构摘要和文档导航。
- `docs/project.yaml` 保存机器可读取的职责、模块、命令和社区信息。
- 架构的“为什么”通过 ADR 或专题设计文档记录。
- 代码正确性由项目测试保障；文档与架构声明的一致性由 Agent 结合源码、POM 和 Git Diff 动态检查。
- 修改模块、依赖方向、领域边界、启动命令、生成流程或运行配置时，应在同一提交中更新相关文档。
