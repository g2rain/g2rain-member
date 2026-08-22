package com.g2rain.member.domain;

import com.g2rain.common.json.JsonCodec;
import com.g2rain.common.json.JsonCodecFactory;
import com.g2rain.common.utils.Strings;
import com.g2rain.member.dto.WechatWorkExternalProfileDto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 企业微信外部资料白名单清洗，仅允许昵称与头像落库至 raw_profile。
 */
public final class WechatWorkExternalProfileSanitizer {

    private static final JsonCodec JSON = JsonCodecFactory.instance();

    private WechatWorkExternalProfileSanitizer() {
    }

    public static String toRawProfileJson(WechatWorkExternalProfileDto profile) {
        if (profile == null) {
            return null;
        }
        Map<String, String> allowed = new LinkedHashMap<>(2);
        if (Strings.isNotBlank(profile.getName())) {
            allowed.put("name", profile.getName().trim());
        }
        if (Strings.isNotBlank(profile.getAvatar())) {
            allowed.put("avatar", profile.getAvatar().trim());
        }
        if (allowed.isEmpty()) {
            return null;
        }
        return JSON.obj2str(allowed);
    }

    public static String extractName(WechatWorkExternalProfileDto profile) {
        if (profile == null || Strings.isBlank(profile.getName())) {
            return null;
        }
        return profile.getName().trim();
    }

    public static String extractAvatar(WechatWorkExternalProfileDto profile) {
        if (profile == null || Strings.isBlank(profile.getAvatar())) {
            return null;
        }
        return profile.getAvatar().trim();
    }
}