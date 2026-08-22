package com.g2rain.member.controller;

import com.g2rain.common.model.Result;
import com.g2rain.member.api.WechatWorkMemberInternalApi;
import com.g2rain.member.dto.WechatWorkMemberResolveRequest;
import com.g2rain.member.service.WechatWorkMemberResolver;
import com.g2rain.member.vo.WechatWorkMemberResolveVo;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业微信会员解析内部控制器。
 */
@RestController
@RequestMapping("/internal/wechat_work_member")
public class WechatWorkMemberInternalController implements WechatWorkMemberInternalApi {

    @Resource(name = "wechatWorkMemberResolverImpl")
    private WechatWorkMemberResolver wechatWorkMemberResolver;

    @Override
    public Result<WechatWorkMemberResolveVo> resolveOrCreate(
        @RequestBody @Validated WechatWorkMemberResolveRequest request
    ) {
        return Result.success(wechatWorkMemberResolver.resolveOrCreate(request));
    }
}