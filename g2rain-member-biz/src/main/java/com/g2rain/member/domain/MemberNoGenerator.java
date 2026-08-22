package com.g2rain.member.domain;

import java.util.Locale;

/**
 * 会员编号生成器：M + Base36(memberId) 大写。
 */
public final class MemberNoGenerator {

    private static final String PREFIX = "M";

    private MemberNoGenerator() {
    }

    public static String generate(long memberId) {
        if (memberId <= 0) {
            throw new IllegalArgumentException("memberId must be positive");
        }
        return PREFIX + Long.toString(memberId, Character.MAX_RADIX)
                .toUpperCase(Locale.ROOT);
    }
}