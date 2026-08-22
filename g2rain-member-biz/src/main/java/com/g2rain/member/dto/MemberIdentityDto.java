package com.g2rain.member.dto;

import com.g2rain.common.model.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.g2rain.common.validation.CreateGroup;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 会员身份表查询DTO
 * 表名: member_identity
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会员身份表 DTO")
public class MemberIdentityDto extends BaseDto {

    /**
     * 机构标识，关联 g2rain_basis.organ.id，身份归属租户
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "机构标识，关联 g2rain_basis.organ.id，身份归属租户（新增必填）")
    private Long organId;

    /**
     * 会员标识，关联 member.id
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "会员标识，关联 member.id（新增必填）")
    private Long memberId;

    /**
     * 身份类型[WECHAT_WORK:企业微信, MOBILE:手机号]
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 32)
    @Schema(description = "身份类型[WECHAT_WORK:企业微信, MOBILE:手机号]（新增必填）", maxLength = 32)
    private String identityType;

    /**
     * 身份值；企业微信为 external_userid，手机号为标准化后的手机号
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 256)
    @Schema(description = "身份值；企业微信为 external_userid，手机号为标准化后的手机号（新增必填）", maxLength = 256)
    private String identityValue;

    /**
     * 验证标识[0:未验证, 1:已验证]
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "验证标识[0:未验证, 1:已验证]（新增必填）")
    private Byte verified;

    /**
     * 外部身份原始资料，JSON 格式
     */
    @Size(max = 1073741824)
    @Schema(description = "外部身份原始资料，JSON 格式", maxLength = 1073741824)
    private String rawProfile;
}
