# CRUD 代码生成

根 POM 配置了 `g2rain-crafter` 的 `bootstrap` Goal，生成参数位于根目录 `codegen.properties`。

## 使用场景

新增或调整会员数据库表，需要生成标准 API、Controller、Service、DAO、DTO、VO、Converter 和 MyBatis Mapper 骨架时使用。生成结果不包含身份唯一性、租户一致性、并发幂等、逻辑删除占位和资料最小化等领域规则。

## 配置文件

当前关键配置：

| 配置 | 当前值/说明 |
| --- | --- |
| `project.basePackage` | `com.g2rain.member` |
| `database.url` | 本地 `g2rain_member` 元数据库连接 |
| `database.tables` | `member,member_identity` |
| `tables.overwrite` | 日常生成保持 `false` |
| `data.isolation.withIsolation` | 为租户表生成隔离能力 |
| `data.isolation.tenantColumns` | `organ_id` |

不得提交生产数据库密码。生成前应把目标表收敛到本次需要处理的范围。

## 配置文件模式

```bash
mvn g2rain-crafter:bootstrap
```

如果本机尚未注册 Maven 插件前缀：

```bash
mvn com.g2rain:g2rain-crafter:1.0.7:bootstrap -Dphase=foundry
```

## 交互式示例

当必要参数未在配置中提供时，Crafter 会在终端逐项询问。典型过程如下（提示文字以实际插件版本为准）：

```text
请输入项目基础包名: com.g2rain.member
请输入数据库地址: jdbc:mysql://localhost:3306/g2rain_member
请输入数据库用户名: root
请输入数据库密码: ******
请输入待生成表（逗号分隔）: member,member_identity
是否覆盖已有文件 [y/N]: N
```

交互式执行仍必须在项目根目录进行。生成结束后：

1. 检查 Git Diff，确认没有覆盖手写领域逻辑。
2. 补充事务、幂等、租户一致性、状态和安全规则。
3. 补充或更新测试与设计文档。
4. 执行 `mvn clean verify`。

## 风险

- 不要为处理单个文件而开启全项目覆盖。
- 企业微信解析代码中的无隔离查询必须保留显式 `organId` 校验。
- `member` 与 `member_identity` 的联合创建必须保持单事务。
- 生成器不会理解历史身份占位、并发唯一键冲突和敏感资料白名单。

