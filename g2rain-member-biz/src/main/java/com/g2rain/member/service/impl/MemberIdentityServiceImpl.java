package com.g2rain.member.service.impl;

import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.id.IdGenerator;
import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.utils.Moments;
import com.g2rain.common.validation.Validations;
import com.g2rain.member.converter.MemberIdentityConverter;
import com.g2rain.member.dao.MemberIdentityDao;
import com.g2rain.member.dao.po.MemberIdentityPo;
import com.g2rain.member.dto.MemberIdentityDto;
import com.g2rain.member.dto.MemberIdentitySelectDto;
import com.g2rain.member.enums.MemberErrorCode;
import com.g2rain.member.service.MemberIdentityService;
import com.g2rain.member.support.MemberPrincipalSupport;
import com.g2rain.member.vo.MemberIdentityVo;
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
 * 会员身份表服务实现类
 * 表名: member_identity
 *
 * @author G2rain Generator
 */
@Service(value = "memberIdentityServiceImpl")
public class MemberIdentityServiceImpl implements MemberIdentityService {

    @Resource(name = "memberIdentityDao")
    private MemberIdentityDao memberIdentityDao;

    private IdGenerator idGenerator;

    @Qualifier("idGenerator")
    @Autowired(required = false)
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<MemberIdentityVo> selectList(MemberIdentitySelectDto selectDto) {
        MemberPrincipalSupport.constrainIdentitySelect(selectDto);
        return memberIdentityDao.selectList(selectDto)
                .stream()
                .map(MemberIdentityConverter.INSTANCE::po2vo)
                .toList();
    }

    @Override
    public PageData<MemberIdentityVo> selectPage(PageSelectListDto<MemberIdentitySelectDto> selectDto) {
        Asserts.isTrue(Objects.nonNull(selectDto) && Objects.nonNull(selectDto.getQuery()),
            SystemErrorCode.PARAM_REQUIRED, "query");
        MemberPrincipalSupport.constrainIdentitySelect(selectDto.getQuery());
        Page<MemberIdentityPo> page = PageContext.of(selectDto.getPageNum(), selectDto.getPageSize(), () -> {
            memberIdentityDao.selectList(selectDto.getQuery());
        });
        List<MemberIdentityVo> result = page.getResult()
                .stream()
                .map(MemberIdentityConverter.INSTANCE::po2vo)
                .toList();
        return PageData.of(page.getPageNum(), page.getPageSize(), page.getTotal(), result);
    }

    @Override
    public Long save(MemberIdentityDto dto) {
        Validations.validateSave(dto);

        if (MemberPrincipalSupport.isMemberSession()) {
            // MEMBER 会话不得自行改绑身份；身份写入由受信编排（如 IAM resolve）完成
            throw new BusinessException(MemberErrorCode.MEMBER_ACCESS_DENIED);
        }

        MemberIdentityPo entity = MemberIdentityConverter.INSTANCE.dto2po(dto);
        Long id = entity.getId();
        if (Objects.isNull(id) || id == 0) {
            entity.setId(idGenerator.generateId());
            LocalDateTime now = Moments.now();
            entity.setUpdateTime(now);
            entity.setCreateTime(now);
            int success = memberIdentityDao.insert(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.CREATE_DATA_ERROR);
        } else {
            MemberIdentityPo existing = memberIdentityDao.selectById(id);
            Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
            entity.setUpdateTime(Moments.now());
            int success = memberIdentityDao.update(entity);
            Asserts.greaterThan(success, 0, SystemErrorCode.UPDATE_DATA_ERROR, id);
        }

        return entity.getId();
    }

    @Override
    public int delete(Long id) {
        if (MemberPrincipalSupport.isMemberSession()) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCESS_DENIED);
        }
        MemberIdentityPo existing = memberIdentityDao.selectById(id);
        Asserts.isTrue(Objects.nonNull(existing), SystemErrorCode.DATA_NOT_EXISTS, id);
        return memberIdentityDao.delete(id);
    }
}
