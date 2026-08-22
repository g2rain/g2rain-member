package com.g2rain.member.converter;

import com.g2rain.common.converter.CommonConverter;
import com.g2rain.member.dao.po.MemberIdentityPo;
import com.g2rain.member.dto.MemberIdentityDto;
import com.g2rain.member.vo.MemberIdentityVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * 会员身份表转换器
 * 用于Po、Vo、Dto之间的相互转换
 * 表名: member_identity
 *
 * @author G2rain Generator
 */
@Mapper(uses = CommonConverter.class)
public interface MemberIdentityConverter {

    /**
     * 单例实例，通过 {@link Mappers#getMapper(Class)} 获取 MapStruct 自动生成的实现。
     */
    MemberIdentityConverter INSTANCE = Mappers.getMapper(MemberIdentityConverter.class);

    /**
     * Po -> Vo
     * 自动将 createTime 和 updateTime 从 {@link LocalDateTime} 转换为 {@link String}
     */
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "localDateTimeToString")
    MemberIdentityVo po2vo(MemberIdentityPo po);

    /**
     * Dto -> Po
     * 自动将 createTime 和 updateTime 从 {@link String} 转换为 {@link LocalDateTime}
     * 忽略 version 字段
     * 忽略 deleteFlag 字段
     */
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleteFlag", ignore = true)
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "updateTime", source = "updateTime", qualifiedByName = "stringToLocalDateTime")
    MemberIdentityPo dto2po(MemberIdentityDto dto);
}
