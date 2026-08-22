package com.g2rain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 企业微信会员解析/创建请求（仅供可信内部调用方使用）。
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "企业微信会员解析请求")
public class WechatWorkMemberResolveRequest {

    @NotNull
    @Positive
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "租户机构标识")
    private Long organId;

    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "企业微信 external_userid")
    private String externalUserId;

    @Valid
    @Schema(description = "外部联系人资料（昵称、头像）")
    private WechatWorkExternalProfileDto externalProfile;
}
