package com.g2rain.member.service.impl;

import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.id.IdGenerator;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.utils.Moments;
import com.g2rain.common.utils.Strings;
import com.g2rain.member.dao.MemberDao;
import com.g2rain.member.dao.MemberIdentityDao;
import com.g2rain.member.dao.po.MemberIdentityPo;
import com.g2rain.member.dao.po.MemberPo;
import com.g2rain.member.domain.MemberNoGenerator;
import com.g2rain.member.domain.WechatWorkExternalProfileSanitizer;
import com.g2rain.member.dto.WechatWorkExternalProfileDto;
import com.g2rain.member.dto.WechatWorkMemberResolveRequest;
import com.g2rain.member.enums.MemberErrorCode;
import com.g2rain.member.enums.MemberIdentityType;
import com.g2rain.member.enums.MemberStatus;
import com.g2rain.member.service.WechatWorkMemberResolver;
import com.g2rain.member.vo.WechatWorkMemberResolveVo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 企业微信会员解析服务实现。
 */
@Service(value = "wechatWorkMemberResolverImpl")
public class WechatWorkMemberResolverImpl implements WechatWorkMemberResolver {

    @Resource(name = "memberDao")
    private MemberDao memberDao;

    @Resource(name = "memberIdentityDao")
    private MemberIdentityDao memberIdentityDao;

    /**
     * 自引用代理，确保 {@link #createMemberAndIdentity} 的 {@code @Transactional} 生效。
     */
    private WechatWorkMemberResolverImpl self;

    private IdGenerator idGenerator;

    @Lazy
    @Autowired
    public void setSelf(WechatWorkMemberResolverImpl self) {
        this.self = self;
    }

    @Qualifier("idGenerator")
    @Autowired(required = false)
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public WechatWorkMemberResolveVo resolveOrCreate(WechatWorkMemberResolveRequest request) {
        Long organId = request.getOrganId();
        Asserts.isTrue(organId != null && organId > 0, MemberErrorCode.MEMBER_ORGAN_INVALID);

        String externalUserId = normalizeExternalUserId(request.getExternalUserId());
        Asserts.isTrue(Strings.isNotBlank(externalUserId), MemberErrorCode.MEMBER_WECHAT_WORK_IDENTITY_INVALID);

        MemberIdentityPo identity = memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
            organId,
            MemberIdentityType.WECHAT_WORK.name(),
            externalUserId
        );
        if (identity != null) {
            return resolveFromIdentity(organId, identity);
        }

        try {
            return self.createMemberAndIdentity(organId, externalUserId, request.getExternalProfile());
        } catch (DuplicateKeyException ex) {
            // 事务已随 createMemberAndIdentity 回滚；回查胜出方身份，避免孤立会员与半成品写入。
            MemberIdentityPo existing = memberIdentityDao.selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
                organId,
                MemberIdentityType.WECHAT_WORK.name(),
                externalUserId
            );
            if (existing == null) {
                throw new BusinessException(MemberErrorCode.MEMBER_IDENTITY_CREATE_CONFLICT);
            }
            return resolveFromIdentity(organId, existing);
        }
    }

    /**
     * 在同一事务中创建会员与企微身份；唯一键冲突必须向外抛出以触发回滚。
     */
    @Transactional
    public WechatWorkMemberResolveVo createMemberAndIdentity(
        Long organId,
        String externalUserId,
        WechatWorkExternalProfileDto externalProfile
    ) {
        LocalDateTime now = Moments.now();
        long memberId = idGenerator.generateId();
        String memberNo = MemberNoGenerator.generate(memberId);

        MemberPo member = new MemberPo();
        member.setId(memberId);
        member.setOrganId(organId);
        member.setMemberNo(memberNo);
        member.setName(WechatWorkExternalProfileSanitizer.extractName(externalProfile));
        member.setAvatar(WechatWorkExternalProfileSanitizer.extractAvatar(externalProfile));
        member.setStatus(MemberStatus.NORMAL.name());
        member.setCreateTime(now);
        member.setUpdateTime(now);

        int memberInserted = memberDao.insertWithoutIsolation(member);
        Asserts.greaterThan(memberInserted, 0, SystemErrorCode.CREATE_DATA_ERROR);

        MemberIdentityPo identity = new MemberIdentityPo();
        identity.setId(idGenerator.generateId());
        identity.setOrganId(organId);
        identity.setMemberId(memberId);
        identity.setIdentityType(MemberIdentityType.WECHAT_WORK.name());
        identity.setIdentityValue(externalUserId);
        identity.setVerified((byte) 1);
        identity.setRawProfile(WechatWorkExternalProfileSanitizer.toRawProfileJson(externalProfile));
        identity.setCreateTime(now);
        identity.setUpdateTime(now);

        int identityInserted = memberIdentityDao.insertWithoutIsolation(identity);
        Asserts.greaterThan(identityInserted, 0, SystemErrorCode.CREATE_DATA_ERROR);

        return buildVo(member, identity, true);
    }

    WechatWorkMemberResolveVo resolveFromIdentity(Long organId, MemberIdentityPo identity) {
        if (Boolean.TRUE.equals(identity.getDeleteFlag())) {
            throw new BusinessException(MemberErrorCode.MEMBER_IDENTITY_DELETED);
        }
        if (!Objects.equals(identity.getOrganId(), organId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_IDENTITY_ORGAN_MISMATCH);
        }

        MemberPo member = memberDao.selectByIdIncludingDeletedWithoutIsolation(identity.getMemberId());
        if (member == null) {
            throw new BusinessException(MemberErrorCode.MEMBER_IDENTITY_BROKEN);
        }
        if (Boolean.TRUE.equals(member.getDeleteFlag())) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        if (!Objects.equals(member.getOrganId(), organId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_IDENTITY_ORGAN_MISMATCH);
        }

        return buildVo(member, identity, false);
    }

    static WechatWorkMemberResolveVo buildVo(MemberPo member, MemberIdentityPo identity, boolean newMember) {
        WechatWorkMemberResolveVo vo = new WechatWorkMemberResolveVo();
        vo.setMemberId(member.getId());
        vo.setMemberNo(member.getMemberNo());
        vo.setMemberStatus(member.getStatus());
        vo.setNewMember(newMember);
        vo.setIdentityVerified(Objects.equals(identity.getVerified(), (byte) 1));
        return vo;
    }

    static String normalizeExternalUserId(String externalUserId) {
        if (externalUserId == null) {
            return null;
        }
        return externalUserId.trim();
    }
}
