package com.g2rain.member.service.impl;

import com.g2rain.common.enums.SessionType;
import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.web.PrincipalContext;
import com.g2rain.common.web.PrincipalContextHolder;
import com.g2rain.member.dao.MemberDao;
import com.g2rain.member.dao.po.MemberPo;
import com.g2rain.member.dto.MemberDto;
import com.g2rain.member.enums.MemberErrorCode;
import com.g2rain.member.vo.MemberVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberServiceImplMemberSessionTest {

    private MemberDao memberDao;
    private MemberServiceImpl service;

    @BeforeEach
    void setUp() {
        memberDao = mock(MemberDao.class);
        service = new MemberServiceImpl();
        ReflectionTestUtils.setField(service, "memberDao", memberDao);
    }

    @Test
    void getCurrentReadsPrincipalMemberId() {
        PrincipalContextHolder.runWith(PrincipalContext.of(), () -> {
            PrincipalContextHolder.setSessionType(SessionType.MEMBER);
            PrincipalContextHolder.setMemberId(9L);
            PrincipalContextHolder.setOrganId(10001L);

            MemberPo po = new MemberPo();
            po.setId(9L);
            po.setOrganId(10001L);
            po.setMemberNo("M9");
            po.setStatus("NORMAL");
            when(memberDao.selectById(9L)).thenReturn(po);

            MemberVo vo = service.getCurrent();
            assertEquals(9L, vo.getId());
            assertEquals("M9", vo.getMemberNo());
            verify(memberDao).selectById(9L);
        });
    }

    @Test
    void saveAsMemberRejectsCreate() {
        PrincipalContextHolder.runWith(PrincipalContext.of(), () -> {
            PrincipalContextHolder.setSessionType(SessionType.MEMBER);
            PrincipalContextHolder.setMemberId(9L);
            PrincipalContextHolder.setOrganId(10001L);

            MemberDto dto = new MemberDto();
            dto.setOrganId(10001L);
            dto.setName("x");

            BusinessException ex = assertThrows(BusinessException.class, () -> service.save(dto));
            assertEquals(MemberErrorCode.MEMBER_ACCESS_DENIED.code(), ex.getErrorCode());
            verify(memberDao, never()).insert(org.mockito.ArgumentMatchers.any());
        });
    }

    @Test
    void saveAsMemberRejectsOtherMemberId() {
        PrincipalContextHolder.runWith(PrincipalContext.of(), () -> {
            PrincipalContextHolder.setSessionType(SessionType.MEMBER);
            PrincipalContextHolder.setMemberId(9L);
            PrincipalContextHolder.setOrganId(10001L);

            MemberDto dto = new MemberDto();
            dto.setId(10L);
            dto.setOrganId(10001L);
            dto.setName("x");

            BusinessException ex = assertThrows(BusinessException.class, () -> service.save(dto));
            assertEquals(MemberErrorCode.MEMBER_ACCESS_DENIED.code(), ex.getErrorCode());
        });
    }

    @Test
    void requireActiveForTokenRejectsFrozenMember() {
        MemberPo po = new MemberPo();
        po.setId(9L);
        po.setOrganId(10001L);
        po.setMemberNo("M9");
        po.setStatus("FROZEN");
        po.setDeleteFlag(false);
        when(memberDao.selectByIdIncludingDeletedWithoutIsolation(9L)).thenReturn(po);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.requireActiveForToken(10001L, 9L));
        assertEquals(MemberErrorCode.MEMBER_FROZEN.code(), ex.getErrorCode());
    }

    @Test
    void requireActiveForTokenReturnsNormalMember() {
        MemberPo po = new MemberPo();
        po.setId(9L);
        po.setOrganId(10001L);
        po.setMemberNo("M9");
        po.setStatus("NORMAL");
        po.setDeleteFlag(false);
        when(memberDao.selectByIdIncludingDeletedWithoutIsolation(9L)).thenReturn(po);

        var vo = service.requireActiveForToken(10001L, 9L);
        assertEquals(9L, vo.getId());
        assertEquals("NORMAL", vo.getStatus());
    }

    @Test
    void deleteDeniedForMemberSession() {
        PrincipalContextHolder.runWith(PrincipalContext.of(), () -> {
            PrincipalContextHolder.setSessionType(SessionType.MEMBER);
            PrincipalContextHolder.setMemberId(9L);
            PrincipalContextHolder.setOrganId(10001L);

            BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(9L));
            assertEquals(MemberErrorCode.MEMBER_ACCESS_DENIED.code(), ex.getErrorCode());
            verify(memberDao, never()).delete(org.mockito.ArgumentMatchers.anyLong());
        });
    }
}
