package com.g2rain.member.service.impl;

import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.id.IdGenerator;
import com.g2rain.member.dao.MemberDao;
import com.g2rain.member.dao.MemberIdentityDao;
import com.g2rain.member.dao.po.MemberIdentityPo;
import com.g2rain.member.dao.po.MemberPo;
import com.g2rain.member.dto.WechatWorkExternalProfileDto;
import com.g2rain.member.dto.WechatWorkMemberResolveRequest;
import com.g2rain.member.enums.MemberErrorCode;
import com.g2rain.member.enums.MemberIdentityType;
import com.g2rain.member.enums.MemberStatus;
import com.g2rain.member.vo.WechatWorkMemberResolveVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WechatWorkMemberResolverImplTest {

    private static final Long ORGAN_ID = 100L;
    private static final String EXTERNAL_USER_ID = "wmExternalUser001";

    private WechatWorkMemberResolverImpl resolver;
    private MemberDao memberDao;
    private MemberIdentityDao memberIdentityDao;
    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        resolver = new WechatWorkMemberResolverImpl();
        memberDao = mock(MemberDao.class);
        memberIdentityDao = mock(MemberIdentityDao.class);
        idGenerator = mock(IdGenerator.class);

        ReflectionTestUtils.setField(resolver, "memberDao", memberDao);
        ReflectionTestUtils.setField(resolver, "memberIdentityDao", memberIdentityDao);
        ReflectionTestUtils.setField(resolver, "idGenerator", idGenerator);
        // 单测无 Spring 代理；自引用指向本实例以走 create 入口（冲突回查在 resolveOrCreate）
        ReflectionTestUtils.setField(resolver, "self", resolver);
    }

    @Test
    void resolveOrCreate_existingIdentity_shouldReturnExistingMember() {
        MemberIdentityPo identity = activeIdentity(1000L, 2000L);
        MemberPo member = activeMember(2000L, MemberStatus.NORMAL.name());

        when(memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            eq(ORGAN_ID), eq(MemberIdentityType.WECHAT_WORK.name()), eq(EXTERNAL_USER_ID)
        )).thenReturn(identity);
        when(memberDao.selectByIdIncludingDeletedWithoutIsolation(2000L)).thenReturn(member);

        WechatWorkMemberResolveVo vo = resolver.resolveOrCreate(request());

        assertEquals(2000L, vo.getMemberId());
        assertEquals("M1HGN0", vo.getMemberNo());
        assertEquals(MemberStatus.NORMAL.name(), vo.getMemberStatus());
        assertFalse(vo.getNewMember());
        assertTrue(vo.getIdentityVerified());
        verify(memberDao, never()).insertWithoutIsolation(any());
    }

    @Test
    void resolveOrCreate_frozenMember_shouldReturnWithoutThrowing() {
        MemberIdentityPo identity = activeIdentity(1000L, 2000L);
        MemberPo member = activeMember(2000L, MemberStatus.FROZEN.name());

        when(memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            eq(ORGAN_ID), eq(MemberIdentityType.WECHAT_WORK.name()), eq(EXTERNAL_USER_ID)
        )).thenReturn(identity);
        when(memberDao.selectByIdIncludingDeletedWithoutIsolation(2000L)).thenReturn(member);

        WechatWorkMemberResolveVo vo = resolver.resolveOrCreate(request());

        assertEquals(MemberStatus.FROZEN.name(), vo.getMemberStatus());
    }

    @Test
    void resolveOrCreate_deletedIdentity_shouldThrowMemberIdentityDeleted() {
        MemberIdentityPo identity = activeIdentity(1000L, 2000L);
        identity.setDeleteFlag(Boolean.TRUE);

        when(memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            eq(ORGAN_ID), eq(MemberIdentityType.WECHAT_WORK.name()), eq(EXTERNAL_USER_ID)
        )).thenReturn(identity);

        BusinessException ex = assertThrows(BusinessException.class, () -> resolver.resolveOrCreate(request()));
        assertEquals(MemberErrorCode.MEMBER_IDENTITY_DELETED.code(), ex.getErrorCode());
    }

    @Test
    void resolveOrCreate_noIdentity_shouldCreateMemberAndIdentity() {
        when(memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            eq(ORGAN_ID), eq(MemberIdentityType.WECHAT_WORK.name()), eq(EXTERNAL_USER_ID)
        )).thenReturn(null);
        when(idGenerator.generateId()).thenReturn(3000L, 4000L);
        when(memberDao.insertWithoutIsolation(any(MemberPo.class))).thenReturn(1);
        when(memberIdentityDao.insertWithoutIsolation(any(MemberIdentityPo.class))).thenReturn(1);

        WechatWorkMemberResolveRequest req = request();
        WechatWorkExternalProfileDto profile = new WechatWorkExternalProfileDto();
        profile.setName("Nick");
        profile.setAvatar("https://example.com/a.png");
        req.setExternalProfile(profile);

        WechatWorkMemberResolveVo vo = resolver.resolveOrCreate(req);

        assertEquals(3000L, vo.getMemberId());
        assertEquals("M2BC", vo.getMemberNo());
        assertEquals(MemberStatus.NORMAL.name(), vo.getMemberStatus());
        assertTrue(vo.getNewMember());
        assertTrue(vo.getIdentityVerified());
        verify(memberDao, times(1)).insertWithoutIsolation(any(MemberPo.class));
        verify(memberIdentityDao, times(1)).insertWithoutIsolation(any(MemberIdentityPo.class));
    }

    @Test
    void resolveOrCreate_duplicateKey_shouldRequeryAndResolveExistingIdentity() {
        MemberIdentityPo existing = activeIdentity(5000L, 6000L);
        MemberPo member = activeMember(6000L, MemberStatus.NORMAL.name());

        when(memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            eq(ORGAN_ID), eq(MemberIdentityType.WECHAT_WORK.name()), eq(EXTERNAL_USER_ID)
        )).thenReturn(null)
            .thenReturn(existing);
        when(idGenerator.generateId()).thenReturn(7000L, 8000L);
        when(memberDao.insertWithoutIsolation(any(MemberPo.class))).thenReturn(1);
        when(memberIdentityDao.insertWithoutIsolation(any(MemberIdentityPo.class)))
            .thenThrow(new DuplicateKeyException("uk_organ_identity"));
        when(memberDao.selectByIdIncludingDeletedWithoutIsolation(6000L)).thenReturn(member);

        WechatWorkMemberResolveVo vo = resolver.resolveOrCreate(request());

        assertEquals(6000L, vo.getMemberId());
        assertFalse(vo.getNewMember());
        verify(memberIdentityDao, times(2)).selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            eq(ORGAN_ID), eq(MemberIdentityType.WECHAT_WORK.name()), eq(EXTERNAL_USER_ID)
        );
    }

    @Test
    void resolveOrCreate_organMismatchOnMember_shouldThrowOrganMismatch() {
        MemberIdentityPo identity = activeIdentity(1000L, 2000L);
        MemberPo member = activeMember(2000L, MemberStatus.NORMAL.name());
        member.setOrganId(999L);

        when(memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            eq(ORGAN_ID), eq(MemberIdentityType.WECHAT_WORK.name()), eq(EXTERNAL_USER_ID)
        )).thenReturn(identity);
        when(memberDao.selectByIdIncludingDeletedWithoutIsolation(2000L)).thenReturn(member);

        BusinessException ex = assertThrows(BusinessException.class, () -> resolver.resolveOrCreate(request()));
        assertEquals(MemberErrorCode.MEMBER_IDENTITY_ORGAN_MISMATCH.code(), ex.getErrorCode());
    }

    @Test
    void resolveOrCreate_brokenMemberReference_shouldThrowIdentityBroken() {
        MemberIdentityPo identity = activeIdentity(1000L, 2000L);

        when(memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            eq(ORGAN_ID), eq(MemberIdentityType.WECHAT_WORK.name()), eq(EXTERNAL_USER_ID)
        )).thenReturn(identity);
        when(memberDao.selectByIdIncludingDeletedWithoutIsolation(2000L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> resolver.resolveOrCreate(request()));
        assertEquals(MemberErrorCode.MEMBER_IDENTITY_BROKEN.code(), ex.getErrorCode());
    }

    @Test
    void resolveOrCreate_deletedMember_shouldThrowMemberNotFound() {
        MemberIdentityPo identity = activeIdentity(1000L, 2000L);
        MemberPo member = activeMember(2000L, MemberStatus.NORMAL.name());
        member.setDeleteFlag(Boolean.TRUE);

        when(memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            eq(ORGAN_ID), eq(MemberIdentityType.WECHAT_WORK.name()), eq(EXTERNAL_USER_ID)
        )).thenReturn(identity);
        when(memberDao.selectByIdIncludingDeletedWithoutIsolation(2000L)).thenReturn(member);

        BusinessException ex = assertThrows(BusinessException.class, () -> resolver.resolveOrCreate(request()));
        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND.code(), ex.getErrorCode());
    }

    @Test
    void normalizeExternalUserId_shouldTrimWithoutChangingCase() {
        assertEquals("AbC-123", WechatWorkMemberResolverImpl.normalizeExternalUserId("  AbC-123  "));
    }

    private static WechatWorkMemberResolveRequest request() {
        WechatWorkMemberResolveRequest request = new WechatWorkMemberResolveRequest();
        request.setOrganId(ORGAN_ID);
        request.setExternalUserId(EXTERNAL_USER_ID);
        return request;
    }

    private static MemberIdentityPo activeIdentity(Long identityId, Long memberId) {
        MemberIdentityPo identity = new MemberIdentityPo();
        identity.setId(identityId);
        identity.setOrganId(ORGAN_ID);
        identity.setMemberId(memberId);
        identity.setIdentityType(MemberIdentityType.WECHAT_WORK.name());
        identity.setIdentityValue(EXTERNAL_USER_ID);
        identity.setVerified((byte) 1);
        identity.setDeleteFlag(Boolean.FALSE);
        return identity;
    }

    private static MemberPo activeMember(Long memberId, String status) {
        MemberPo member = new MemberPo();
        member.setId(memberId);
        member.setOrganId(ORGAN_ID);
        member.setMemberNo("M1HGN0");
        member.setStatus(status);
        member.setDeleteFlag(Boolean.FALSE);
        return member;
    }
}