package com.g2rain.member.enums;

import com.g2rain.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 会员模块业务错误码。
 */
@Schema(description = "会员模块错误码枚举")
public enum MemberErrorCode implements ErrorCode {

    @Schema(description = "租户标识无效")
    MEMBER_ORGAN_INVALID("member.40001", "租户标识无效"),

    @Schema(description = "企业微信外部联系人标识无效")
    MEMBER_WECHAT_WORK_IDENTITY_INVALID("member.40002", "企业微信外部联系人标识无效"),

    @Schema(description = "身份指向的会员不存在")
    MEMBER_IDENTITY_BROKEN("member.40003", "身份指向的会员不存在"),

    @Schema(description = "身份与会员租户不一致")
    MEMBER_IDENTITY_ORGAN_MISMATCH("member.40004", "身份与会员租户不一致"),

    @Schema(description = "企业微信身份已逻辑删除")
    MEMBER_IDENTITY_DELETED("member.40005", "企业微信身份已逻辑删除，需人工恢复"),

    @Schema(description = "会员已冻结")
    MEMBER_FROZEN("member.40006", "会员已冻结"),

    @Schema(description = "会员或身份已删除")
    MEMBER_NOT_FOUND("member.40007", "会员不存在或已删除"),

    @Schema(description = "并发创建后仍无法回查身份")
    MEMBER_IDENTITY_CREATE_CONFLICT("member.40008", "会员身份创建冲突，请稍后重试"),

    @Schema(description = "会员会话无权访问该资源")
    MEMBER_ACCESS_DENIED("member.40301", "无权访问该会员资源");

    private final String code;

    private final String messageTemplate;

    MemberErrorCode(String code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String messageTemplate() {
        return messageTemplate;
    }
}
