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
 * 会员表查询DTO
 * 表名: member
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会员表 DTO")
public class MemberDto extends BaseDto {

    /**
     * 机构标识，关联 g2rain_basis.organ.id，会员归属租户
     */
    @NotNull(groups = CreateGroup.class)
    @Schema(description = "机构标识，关联 g2rain_basis.organ.id，会员归属租户（新增必填）")
    private Long organId;

    /**
     * 会员编号，机构内唯一，由系统生成
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 64)
    @Schema(description = "会员编号，机构内唯一，由系统生成（新增必填）", maxLength = 64)
    private String memberNo;

    /**
     * 会员名称
     */
    @Size(max = 128)
    @Schema(description = "会员名称", maxLength = 128)
    private String name;

    /**
     * 会员头像地址
     */
    @Size(max = 512)
    @Schema(description = "会员头像地址", maxLength = 512)
    private String avatar;

    /**
     * 会员手机号，仅作为资料字段，登录身份以 member_identity 为准
     */
    @Size(max = 32)
    @Schema(description = "会员手机号，仅作为资料字段，登录身份以 member_identity 为准", maxLength = 32)
    private String mobile;

    /**
     * 会员状态[NORMAL:正常, FROZEN:冻结]
     */
    @NotBlank(groups = CreateGroup.class)
    @Size(max = 32)
    @Schema(description = "会员状态[NORMAL:正常, FROZEN:冻结]（新增必填）", maxLength = 32)
    private String status;
}
