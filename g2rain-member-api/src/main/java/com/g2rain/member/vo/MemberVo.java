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
 * 会员表返回VO
 * 关联表名: member
 * 功能：封装接口返回数据，继承BaseVo复用基础字段逻辑，隔离数据库实体与前端展示层
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会员表 VO")
public class MemberVo extends BaseVo {

    /**
     * 机构标识，关联 g2rain_basis.organ.id，会员归属租户
     */
    @Schema(description = "机构标识，关联 g2rain_basis.organ.id，会员归属租户")
    private Long organId;

    /**
     * 会员编号，机构内唯一，由系统生成
     */
    @Schema(description = "会员编号，机构内唯一，由系统生成")
    private String memberNo;

    /**
     * 会员名称
     */
    @Schema(description = "会员名称")
    private String name;

    /**
     * 会员头像地址
     */
    @Schema(description = "会员头像地址")
    private String avatar;

    /**
     * 会员手机号，仅作为资料字段，登录身份以 member_identity 为准
     */
    @Schema(description = "会员手机号，仅作为资料字段，登录身份以 member_identity 为准")
    private String mobile;

    /**
     * 会员状态[NORMAL:正常, FROZEN:冻结]
     */
    @Schema(description = "会员状态[NORMAL:正常, FROZEN:冻结]")
    private String status;

    /**
     * 删除标识[0:未删除, 1:已删除]
     */
    @Schema(description = "删除标识（0 未删除，1 已删除）", example = "false")
    @ConditionalJsonIgnore(adminCompany = AdminCompanyCondition.TRUE)
    private Boolean deleteFlag;
}