package com.g2rain.member.dao.po;

import com.g2rain.common.model.BasePo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * 会员身份表返回Po
 * 关联表名: member_identity
 * 功能：封装实体数据，继承BasePo复用基础字段逻辑
 *
 * @author G2rain Generator
 */
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MemberIdentityPo extends BasePo {

    /**
     * 机构标识，关联 g2rain_basis.organ.id，身份归属租户
     */
    private Long organId;

    /**
     * 会员标识，关联 member.id
     */
    private Long memberId;

    /**
     * 身份类型[WECHAT_WORK:企业微信, MOBILE:手机号]
     */
    private String identityType;

    /**
     * 身份值；企业微信为 external_userid，手机号为标准化后的手机号
     */
    private String identityValue;

    /**
     * 验证标识[0:未验证, 1:已验证]
     */
    private Byte verified;

    /**
     * 外部身份原始资料，JSON 格式
     */
    private String rawProfile;

    /**
     * 删除标识[0:未删除, 1:已删除]
     */
    private Boolean deleteFlag;
}