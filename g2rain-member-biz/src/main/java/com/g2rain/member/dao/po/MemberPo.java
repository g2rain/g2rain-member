package com.g2rain.member.dao.po;

import com.g2rain.common.model.BasePo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * 会员表返回Po
 * 关联表名: member
 * 功能：封装实体数据，继承BasePo复用基础字段逻辑
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MemberPo extends BasePo {

    /**
     * 机构标识，关联 g2rain_basis.organ.id，会员归属租户
     */
    private Long organId;

    /**
     * 会员编号，机构内唯一，由系统生成
     */
    private String memberNo;

    /**
     * 会员名称
     */
    private String name;

    /**
     * 会员头像地址
     */
    private String avatar;

    /**
     * 会员手机号，仅作为资料字段，登录身份以 member_identity 为准
     */
    private String mobile;

    /**
     * 会员状态[NORMAL:正常, FROZEN:冻结]
     */
    private String status;

    /**
     * 删除标识[0:未删除, 1:已删除]
     */
    private Boolean deleteFlag;
}