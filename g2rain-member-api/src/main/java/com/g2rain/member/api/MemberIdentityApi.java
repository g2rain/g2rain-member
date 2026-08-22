package com.g2rain.member.api;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.member.dto.MemberIdentitySelectDto;
import com.g2rain.member.vo.MemberIdentityVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


/**
 * 会员身份表API接口
 * 表名: member_identity
 *
 * @author G2rain Generator
 */
@Tag(name = "会员身份表", description = "会员身份表相关接口")
public interface MemberIdentityApi {

    /**
     * 根据条件查询列表
     *
     * @param selectDto 查询条件DTO
     * @return 数据列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询会员身份表列表", description = "根据查询条件返回会员身份表列表")
    Result<List<MemberIdentityVo>> selectList(MemberIdentitySelectDto selectDto);

    /**
     * 根据条件分页查询
     *
     * @param selectDto 查询条件DTO（包含分页参数）
     * @return 分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询会员身份表列表", description = "分页查询会员身份表列表")
    Result<PageData<MemberIdentityVo>> selectPage(PageSelectListDto<MemberIdentitySelectDto> selectDto);
}