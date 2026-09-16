package com.g2rain.member.support;

import com.g2rain.common.enums.SessionType;
import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.web.PrincipalContext;
import com.g2rain.common.web.PrincipalContextHolder;
import com.g2rain.member.dto.MemberIdentitySelectDto;
import com.g2rain.member.dto.MemberSelectDto;
import com.g2rain.member.enums.MemberErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberPrincipalSupportTest {

    @Test
    void constrainMemberSelectForcesPrincipalIdentity() {
        PrincipalContextHolder.runWith(PrincipalContext.of(), () -> {
            PrincipalContextHolder.setSessionType(SessionType.MEMBER);
            PrincipalContextHolder.setMemberId(9L);
            PrincipalContextHolder.setOrganId(10001L);

            MemberSelectDto select = new MemberSelectDto();
            select.setId(999L);
            select.setOrganId(888L);

            MemberPrincipalSupport.constrainMemberSelect(select);

            assertEquals(9L, select.getId());
            assertEquals(10001L, select.getOrganId());
            assertEquals(null, select.getIds());
        });
    }

    @Test
    void constrainIdentitySelectForcesPrincipalMemberId() {
        PrincipalContextHolder.runWith(PrincipalContext.of(), () -> {
            PrincipalContextHolder.setSessionType(SessionType.MEMBER);
            PrincipalContextHolder.setMemberId(9L);
            PrincipalContextHolder.setOrganId(10001L);

            MemberIdentitySelectDto select = new MemberIdentitySelectDto();
            select.setMemberId(777L);
            select.setOrganId(888L);

            MemberPrincipalSupport.constrainIdentitySelect(select);

            assertEquals(9L, select.getMemberId());
            assertEquals(10001L, select.getOrganId());
        });
    }

    @Test
    void assertOwnsMemberResourceRejectsCrossMember() {
        PrincipalContextHolder.runWith(PrincipalContext.of(), () -> {
            PrincipalContextHolder.setSessionType(SessionType.MEMBER);
            PrincipalContextHolder.setMemberId(9L);
            PrincipalContextHolder.setOrganId(10001L);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> MemberPrincipalSupport.assertOwnsMemberResource(10001L, 10L));
            assertEquals(MemberErrorCode.MEMBER_ACCESS_DENIED.code(), ex.getErrorCode());
        });
    }

    @Test
    void bindOrRejectDtoMemberIdRejectsMismatch() {
        PrincipalContextHolder.runWith(PrincipalContext.of(), () -> {
            PrincipalContextHolder.setSessionType(SessionType.MEMBER);
            PrincipalContextHolder.setMemberId(9L);
            PrincipalContextHolder.setOrganId(10001L);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> MemberPrincipalSupport.bindOrRejectDtoMemberId(10L));
            assertEquals(MemberErrorCode.MEMBER_ACCESS_DENIED.code(), ex.getErrorCode());
            assertEquals(9L, MemberPrincipalSupport.bindOrRejectDtoMemberId(null));
            assertEquals(9L, MemberPrincipalSupport.bindOrRejectDtoMemberId(9L));
        });
    }

    @Test
    void nonMemberSessionSkipsOwnershipCheck() {
        PrincipalContextHolder.runWith(PrincipalContext.of(), () -> {
            PrincipalContextHolder.setSessionType(SessionType.USER);
            PrincipalContextHolder.setUserId(1L);
            PrincipalContextHolder.setOrganId(10001L);

            MemberPrincipalSupport.assertOwnsMemberResource(10001L, 999L);
            assertTrue(true);
        });
    }
}
