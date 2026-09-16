package com.g2rain.member.api;

import com.g2rain.common.model.Result;
import com.g2rain.member.dto.WechatWorkMemberResolveRequest;
import com.g2rain.member.vo.WechatWorkMemberResolveVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 企业微信会员解析内部 API。
 * <p><b>约定调用方：</b>{@code g2rain-iam}（受信服务网络内无鉴权直连）。
 * 企业微信智能客服模块及其他业务服务不得直连本接口；客服应调用 IAM
 * {@code POST /auth/member/token}。</p>
 */
@Tag(name = "企业微信会员解析（内部）", description = "约定由 IAM 在受信服务网络内直连：按 organ + external_userid 解析或创建会员")
public interface WechatWorkMemberInternalApi {

    /**
     * 解析或创建企业微信会员。
     */
    @PostMapping("/resolve_or_create")
    @Operation(
        summary = "解析或创建企业微信会员",
        description = "约定由 g2rain-iam 在受信服务网络内无鉴权直连；按租户与企业微信 external_userid 查询身份，不存在则同事务创建会员与身份",
        hidden = true
    )
    Result<WechatWorkMemberResolveVo> resolveOrCreate(
        @RequestBody @Validated WechatWorkMemberResolveRequest request);
}
