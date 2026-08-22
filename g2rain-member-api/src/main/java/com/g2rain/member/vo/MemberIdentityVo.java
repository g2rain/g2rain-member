package com.g2rain.member.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.g2rain.common.model.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;

import com.g2rain.common.json.ConditionalJsonIgnore;
import com.g2rain.common.json.AdminCompanyCondition;

/**
 * 会员身份表返回VO
 * 关联表名: member_identity
 * 功能：封装接口返回数据，继承BaseVo复用基础字段逻辑，隔离数据库实体与前端展示层
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会员身份表 VO")
public class MemberIdentityVo extends BaseVo {

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

    /**
     * 删除标识[0:未删除, 1:已删除]
     */
    @Schema(description = "删除标识（0 未删除，1 已删除）", example = "false")
    @ConditionalJsonIgnore(adminCompany = AdminCompanyCondition.TRUE)
    private Boolean deleteFlag;
}