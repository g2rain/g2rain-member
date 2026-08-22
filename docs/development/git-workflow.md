# Git 分支与提交策略

## 长期分支

| 分支 | 定位 | 允许的主要变更 |
| --- | --- | --- |
| `main` | 稳定主分支，对应可发布、可部署版本 | 已通过测试环境验证的发布内容、版本号升级和发布修复 |
| `develop` | 日常开发集成分支，对应下一次待发布版本 | 已完成开发和本地验证的特性、缺陷修复及配套文档 |

`main` 和 `develop` 都是长期分支。日常功能开发和缺陷修复不得直接在这两个分支上提交业务代码，应从合适的基线创建短期分支并通过 Pull Request 合并。

## 短期分支

| 命名 | 用途 | 示例 |
| --- | --- | --- |
| `feature/<name>` | 新功能、能力扩展或较完整的功能调整 | `feature/member-mobile-binding` |
| `fix/<name>` | 缺陷修复 | `fix/member-identity-conflict` |

分支名使用简短、可读的英文 kebab-case，表达业务目的，不使用开发者姓名或没有含义的编号作为唯一名称。

## 合并流程

```text
feature/* ─┐
           ├─→ develop ─→ 测试环境部署与验证 ─→ main
fix/* ─────┘
```

1. 从最新的 `develop` 创建 `feature/*` 或 `fix/*` 分支。
2. 在短期分支完成代码、测试和文档，并执行项目验证命令。
3. 通过 Pull Request 合并到 `develop`，处理评审意见和持续集成失败。
4. 将 `develop` 部署到测试环境，完成相关功能、回归和集成验证。
5. 测试环境验证通过后，通过 Pull Request 将已验证的 `develop` 变更合并到 `main`。
6. 不允许 `feature/*` 或 `fix/*` 绕过 `develop` 直接合并到 `main`。

如果测试环境发现问题，应继续通过 `fix/*` 修复并合并到 `develop`，重新验证后再进入 `main`，不得只在 `main` 上补改而让两个长期分支产生未解释的差异。

## 提交要求

- 一个提交只处理一个清晰目的，避免把无关格式化、重构和功能修改混在一起。
- 提交说明应使用清晰的动词和范围描述改动意图；推荐格式为 `type(scope): summary`。
- 常用类型包括 `feat`、`fix`、`docs`、`refactor`、`test`、`build` 和 `chore`。
- 功能或缺陷提交应包含必要的测试；改变架构、配置、生成流程或使用方式时同步更新文档。
- 提交 Pull Request 前执行：

```bash
mvn clean verify
```

示例：

```text
feat(member): support mobile identity binding
fix(identity): handle concurrent member creation
docs(workflow): document branch and release strategy
```

## Pull Request 要求

- PR 目标分支与流程一致：短期分支以 `develop` 为目标，测试环境验证后的发布 PR 以 `main` 为目标。
- PR 描述应说明变更目的、影响范围、验证方式、配置或数据变更以及必要的回滚方案。
- 合并前确保构建、测试、静态检查和文档检查通过。
- 涉及数据库、公开 API、内部服务契约或身份安全边界时，应列出兼容性和部署顺序。

## 版本号策略

- `main` 承担正式版本发布，合并发布内容时可以升级 Maven、npm 或其他项目版本号。
- `develop` 和短期分支围绕下一版本开发，避免无目的、反复修改正式版本号。
- `g2rain-common`、`g2rain-spring-boot-starter` 等向其他项目提供 JAR 的仓库，发布到 `main` 时应特别检查并按兼容性升级版本号，确保依赖方能够明确引用新产物。
- 破坏兼容性的变更、新功能和向后兼容修复应采用团队统一的主版本、次版本和修订版本规则。
- 版本升级应与实际发布内容一起评审，并在发布说明中记录重要变更、兼容性和升级步骤。

