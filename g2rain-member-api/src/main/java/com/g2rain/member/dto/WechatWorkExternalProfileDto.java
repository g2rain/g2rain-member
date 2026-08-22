package com.g2rain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 企业微信外部联系人脱敏资料（允许落库字段）。
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "企业微信外部联系人资料（昵称、头像等）")
public class WechatWorkExternalProfileDto {

    @Schema(description = "昵称")
    private String name;

    @Schema(description = "头像地址")
    private String avatar;
}
