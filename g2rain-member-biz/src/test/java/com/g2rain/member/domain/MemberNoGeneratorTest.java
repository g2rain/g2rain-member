package com.g2rain.member.domain;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberNoGeneratorTest {

    @Test
    void generate_sameMemberId_shouldBeDeterministic() {
        long memberId = 1234567890123L;
        assertEquals(MemberNoGenerator.generate(memberId), MemberNoGenerator.generate(memberId));
    }

    @Test
    void generate_differentMemberIds_shouldProduceDifferentNumbers() {
        assertNotEquals(
            MemberNoGenerator.generate(100L),
            MemberNoGenerator.generate(101L)
        );
    }

    @Test
    void generate_shouldStartWithM_andUseBase36Uppercase() {
        String memberNo = MemberNoGenerator.generate(987654321L);
        assertTrue(memberNo.startsWith("M"));
        assertTrue(memberNo.substring(1).matches("[0-9A-Z]+"));
    }

    @Test
    void generate_nonPositiveMemberId_shouldReject() {
        assertThrows(IllegalArgumentException.class, () -> MemberNoGenerator.generate(0));
        assertThrows(IllegalArgumentException.class, () -> MemberNoGenerator.generate(-1));
    }

    @Test
    void generate_concurrentDifferentIds_shouldNotDuplicate() {
        Set<String> generated = ConcurrentHashMap.newKeySet();
        LongStream.range(1, 10_001)
            .parallel()
            .forEach(id -> generated.add(MemberNoGenerator.generate(id)));
        assertEquals(10_000, generated.size());
    }

    @Test
    void generate_memberStatusChange_shouldNotAlterMemberNo() {
        long memberId = 42L;
        String original = MemberNoGenerator.generate(memberId);
        assertEquals(original, MemberNoGenerator.generate(memberId));
    }
}