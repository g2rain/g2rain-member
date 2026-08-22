package com.g2rain.member.api;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.member.dto.MemberSelectDto;
import com.g2rain.member.vo.MemberVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


/**
 * 会员表API接口
 * 表名: member
 *
 * @author G2rain Generator
 */
@Tag(name = "会员表", description = "会员表相关接口")
public interface MemberApi {

    /**
     * 根据条件查询列表
     *
     * @param selectDto 查询条件DTO
     * @return 数据列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询会员表列表", description = "根据查询条件返回会员表列表")
    Result<List<MemberVo>> selectList(MemberSelectDto selectDto);

    /**
     * 根据条件分页查询
     *
     * @param selectDto 查询条件DTO（包含分页参数）
     * @return 分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询会员表列表", description = "分页查询会员表列表")
    Result<PageData<MemberVo>> selectPage(PageSelectListDto<MemberSelectDto> selectDto);
}