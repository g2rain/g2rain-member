package com.g2rain.member.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.g2rain.common.model.BaseSelectListDto;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 会员身份表查询入参DTO
 * 用于MemberIdentityDao.selectList方法的条件筛选
 * 表名: member_identity
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会员身份表查询入参 DTO")
public class MemberIdentitySelectDto extends BaseSelectListDto {

    /**
     * 机构标识，关联 g2rain_basis.organ.id，身份归属租户
     */
    @Schema(description = "机构标识，关联 g2rain_basis.organ.id，身份归属租户")
    private Long organId;

    /**
     * 会员标识，关联 member.id
     */
    @Schema(description = "会员标识，关联 member.id")
    private Long memberId;

    /**
     * 身份类型[WECHAT_WORK:企业微信, MOBILE:手机号]
     */
    @Schema(description = "身份类型[WECHAT_WORK:企业微信, MOBILE:手机号]")
    private String identityType;

    /**
     * 身份值；企业微信为 external_userid，手机号为标准化后的手机号
     */
    @Schema(description = "身份值；企业微信为 external_userid，手机号为标准化后的手机号")
    private String identityValue;

    /**
     * 验证标识[0:未验证, 1:已验证]
     */
    @Schema(description = "验证标识[0:未验证, 1:已验证]")
    private Byte verified;

    /**
     * 外部身份原始资料，JSON 格式
     */
    @Schema(description = "外部身份原始资料，JSON 格式")
    private String rawProfile;
}