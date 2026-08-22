package com.g2rain.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 企业微信会员身份解析结果。
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "企业微信会员解析结果")
public class WechatWorkMemberResolveVo {

    @Schema(description = "会员标识")
    private Long memberId;

    @Schema(description = "会员编号")
    private String memberNo;

    @Schema(description = "会员状态[NORMAL:正常, FROZEN:冻结]")
    private String memberStatus;

    @Schema(description = "是否本次新创建会员")
    private Boolean newMember;

    @Schema(description = "企业微信身份是否已验证")
    private Boolean identityVerified;
}
