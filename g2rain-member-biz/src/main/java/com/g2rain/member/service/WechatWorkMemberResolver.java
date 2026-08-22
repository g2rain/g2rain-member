package com.g2rain.member.service;

import com.g2rain.member.dto.WechatWorkMemberResolveRequest;
import com.g2rain.member.vo.WechatWorkMemberResolveVo;

/**
 * 企业微信会员解析服务。
 */
public interface WechatWorkMemberResolver {

    /**
     * 按租户与企业微信 external_userid 解析或创建会员。
     */
    WechatWorkMemberResolveVo resolveOrCreate(WechatWorkMemberResolveRequest request);
}