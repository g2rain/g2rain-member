# 会员编号生成规范

## 1. 目标

`member_no` 是会员对业务侧展示的稳定编号，用于在不暴露手机号、企业微信身份等登录凭据的情况下识别会员。

第一版采用无业务含义的系统编号，不引入按机构递增的流水号、日期分段或数据库自增依赖。

## 2. 编号格式

```text
M + Base36(member.id).toUpperCase()
```

- `M`：会员编号固定前缀。
- `member.id`：使用项目现有的全局唯一 ID 生成机制产生。
- `Base36`：使用字符 `0-9A-Z` 对会员 ID 进行 36 进制编码。
- 编号不补齐固定长度，不包含机构、日期、手机号等业务信息。

示意：

```text
member.id = <全局唯一正整数>
member_no = M4LDQPDM6B4O
```

## 3. 生成流程

1. 使用项目现有 ID 生成器生成正数 `member.id`。
2. 将该 ID 转换为大写 Base36 字符串。
3. 添加固定前缀 `M`，得到 `member_no`。
4. 在同一事务中写入 `member`。
5. 由数据库唯一索引 `uk_organ_member_no (organ_id, member_no)` 做最终一致性校验。

数据库使用大小写敏感排序规则 `utf8mb4_0900_as_cs`。生成器必须始终输出大写编号，查询时不得自动转换或接受大小写不同的编号作为同一会员编号。

生成逻辑必须集中在会员领域服务或专用生成器中，不允许各入口自行拼接。企业微信首次入会、手机号首次入会等入口应复用同一逻辑。

Java 实现示例：

```java
import java.util.Locale;

public final class MemberNoGenerator {

    private static final String PREFIX = "M";

    private MemberNoGenerator() {
    }

    public static String generate(long memberId) {
        if (memberId <= 0) {
            throw new IllegalArgumentException("memberId must be positive");
        }
        return PREFIX + Long.toString(memberId, Character.MAX_RADIX)
                .toUpperCase(Locale.ROOT);
    }
}
```

## 4. 唯一性与并发

- 全局 ID 唯一时，由其确定性转换得到的 `member_no` 也全局唯一，因此自然满足机构内唯一要求。
- 生成过程不访问数据库序列表，也不依赖单机锁，适用于多实例并发创建会员。
- `member_no` 只能由已确定的 `member.id` 派生；禁止独立随机生成后反复碰撞重试。
- 如果插入时发生唯一键冲突，应视为 ID 生成或历史数据异常并记录错误，不应静默改号。

## 5. 生命周期规则

- `member_no` 创建后不可修改。
- 会员逻辑删除后，原编号不得分配给其他会员。
- 会员冻结、绑定或解绑身份时，编号保持不变。
- 合并重复会员时，保留目标会员的编号；被合并会员的编号只随原记录留存，不复用。
- `member_no` 不作为登录凭据，登录和身份识别统一通过 `member_identity` 完成。

## 6. 禁止事项

`member_no` 不得直接使用或包含以下信息：

- 手机号、邮箱等个人信息。
- 企业微信 `external_userid` 等外部身份值。
- 可变的机构编号或机构名称。
- 会员等级、来源渠道等业务状态。
- 依赖本地内存计数器生成的流水号。

## 7. 测试要求

- 相同 `member.id` 必须始终生成相同编号。
- 不同正数 ID 必须生成不同编号。
- 生成结果必须以 `M` 开头，且其余字符只能是 `0-9A-Z`。
- `member.id` 小于或等于 0 时必须拒绝生成。
- 并发生成不同 ID 的会员编号时不得出现重复。
- 会员状态和身份绑定发生变化时，`member_no` 不得变化。

## 8. 后续扩展

如果未来明确需要 `M202608190001` 一类可读连续编号，应另行设计按 `organ_id + 日期` 分段的持久化取号机制，并评估并发、回滚、断号和多实例一致性问题。在此之前继续使用本规范，不在当前生成规则中预留业务字段。
