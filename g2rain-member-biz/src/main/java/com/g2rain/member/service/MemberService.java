package com.g2rain.member.service;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.member.dto.MemberDto;
import com.g2rain.member.dto.MemberSelectDto;
import com.g2rain.member.vo.MemberVo;

import java.util.List;

/**
 * 会员表服务接口
 * 表名: member
 *
 * @author G2rain Generator
 */
public interface MemberService {

    /**
     * 根据条件查询列表
     *
     * @param selectDto 查询条件DTO
     * @return VO对象列表
     */
    List<MemberVo> selectList(MemberSelectDto selectDto);

    /**
     * 根据条件分页查询
     *
     * @param selectDto 查询条件DTO（包含分页参数）
     * @return 分页VO数据
     */
    PageData<MemberVo> selectPage(PageSelectListDto<MemberSelectDto> selectDto);

    /**
     * 新增或更新数据
     *
     * @param dto 数据传输对象
     * @return 操作结果（影响行数）
     */
    Long save(MemberDto dto);

    /**
     * 根据ID删除数据
     *
     * @param id 主键ID
     * @return 操作结果（影响行数）
     */
    int delete(Long id);
}