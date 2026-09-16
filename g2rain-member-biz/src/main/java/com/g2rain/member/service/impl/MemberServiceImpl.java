package com.g2rain.member.service.impl;

import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.id.IdGenerator;
import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.utils.Moments;
import com.g2rain.common.validation.Validations;
import com.g2rain.member.converter.MemberConverter;
import com.g2rain.member.dao.MemberDao;
import com.g2rain.member.dao.po.MemberPo;
import com.g2rain.member.dto.MemberDto;
import com.g2rain.member.dto.MemberSelectDto;
import com.g2rain.member.enums.MemberErrorCode;
import com.g2rain.member.enums.MemberStatus;
import com.g2rain.member.service.MemberService;
import com.g2rain.member.support.MemberPrincipalSupport;
import com.g2rain.member.vo.MemberVo;
import com.g2rain.mybatis.pagination.PageContext;
import com.g2rain.mybatis.pagination.model.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 会员表服务实现类
 * 表名: member
 *
 * @author G2rain Generator
 */
@Service(value = "memberServiceImpl")
public class MemberServiceImpl implements MemberService {

    @Resource(name = "memberDao")
    private MemberDao memberDao;

    private IdGenerator idGenerator;

    @Qualifier("idGenerator")
    @Autowired(required = false)
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<MemberVo> selectList(MemberSelectDto selectDto) {
        MemberPrincipalSupport.constrainMemberSelect(selectDto);
        return memberDao.selectList(selectDto)
                .stream()
                .map(MemberConverter.INSTANCE::po2vo)
                .toList();
    }

    @Override
    public PageData<MemberVo> selectPage(PageSelectListDto<MemberSelectDto> selectDto) {
        Asserts.isTrue(Objects.nonNull(selectDto) && Objects.nonNull(selectDto.getQuery()),
            SystemErrorCode.PARAM_REQUIRED, "query");
        MemberPrincipalSupport.constrainMemberSelect(selectDto.getQuery());
        Page<MemberPo> page = PageContext.of(selectDto.getPageNum(), selectDto.getPageSize(), () -> {
            memberDao.selectList(selectDto.getQuery());
        });
        List<MemberVo> result = page.getResult()
                .stream()
                .map(MemberConverter.INSTANCE::po2vo)
                .toList();
        return PageData.of(page.getPageNum(), page.getPageSize(), page.getTotal(), result);
    }

    @Override
    public MemberVo getCurrent() {
        Long memberId = MemberPrincipalSupport.requireMemberId();
        MemberPo existing = memberDao.selectById(memberId);
        Asserts.isTrue(Objects.nonNull(existing), MemberErrorCode.MEMBER_NOT_FOUND, memberId);
        MemberPrincipalSupport.assertOwnsMemberResource(existing.getOrganId(), existing.getId());
        return MemberConverter.INSTANCE.po2vo(existing);
    }

    @Override
    public MemberVo requireActiveForToken(Long organId, Long memberId) {
        Asserts.isTrue(organId != null && organId > 0L, MemberErrorCode.MEMBER_ORGAN_INVALID);
        Asserts.isTrue(memberId != null && memberId > 0L, SystemErrorCode.PARAM_VAL_INVALID, memberId);

        MemberPo member = memberDao.selectByIdIncludingDeletedWithoutIsolation(memberId);
        if (member == null || Boolean.TRUE.equals(member.getDeleteFlag())) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        if (!Objects.equals(member.getOrganId(), organId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_IDENTITY_ORGAN_MISMATCH);
        }
        if (!MemberStatus.NORMAL.name().equals(member.getStatus())) {
            throw new BusinessException(MemberErrorCode.MEMBER_FROZEN);
        }

        return MemberConverter.INSTANCE.po2vo(member);
    }

    @Override
    public Long save(MemberDto dto) {
        if (MemberPrincipalSupport.isMemberSession()) {
            return saveAsMember(dto);
        }

        Validations.validateSave(dto);

        MemberPo entity = MemberConverter.INSTANCE.dto2po(dto);
        Long id = entity.getId();
        if (Objects.isNull(id) || id == 0) {
            entity.setId(idGenerator.generateId());
            LocalDateTime now = Moments.now();
            entity.setUpdateTime(now);
            entity.setCreateTime(now);
            int success = memberDao.insert(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.CREATE_DATA_ERROR);
        } else {
            MemberPo existing = memberDao.selectById(id);
            Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
            entity.setUpdateTime(Moments.now());
            int success = memberDao.update(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        }

        return entity.getId();
    }

    /**
     * MEMBER 会话仅允许更新自己的资料；禁止新增/伪造 organId。
     */
    private Long saveAsMember(MemberDto dto) {
        Long principalMemberId = MemberPrincipalSupport.requireMemberId();
        Long id = dto == null ? null : dto.getId();
        if (Objects.isNull(id) || id == 0) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCESS_DENIED);
        }
        if (!Objects.equals(id, principalMemberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCESS_DENIED);
        }

        Validations.validateSave(dto);
        Long principalOrganId = MemberPrincipalSupport.bindOrRejectDtoOrganId(dto.getOrganId());

        MemberPo existing = memberDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), MemberErrorCode.MEMBER_NOT_FOUND, id);
        MemberPrincipalSupport.assertOwnsMemberResource(existing.getOrganId(), existing.getId());

        MemberPo entity = MemberConverter.INSTANCE.dto2po(dto);
        entity.setId(principalMemberId);
        entity.setOrganId(principalOrganId);
        entity.setMemberNo(existing.getMemberNo());
        entity.setStatus(existing.getStatus());
        entity.setUpdateTime(Moments.now());
        int success = memberDao.update(entity);
        Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        return principalMemberId;
    }

    @Override
    public int delete(Long id) {
        if (MemberPrincipalSupport.isMemberSession()) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCESS_DENIED);
        }
        MemberPo existing = memberDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        return memberDao.delete(id);
    }
}
