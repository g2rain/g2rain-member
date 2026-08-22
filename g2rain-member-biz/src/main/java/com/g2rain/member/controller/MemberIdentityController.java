package com.g2rain.member.controller;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.member.api.MemberIdentityApi;
import com.g2rain.member.dto.MemberIdentityDto;
import com.g2rain.member.dto.MemberIdentitySelectDto;
import com.g2rain.member.service.MemberIdentityService;
import com.g2rain.member.vo.MemberIdentityVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会员身份表控制器
 * 表名: member_identity
 *
 * @author G2rain Generator
 */
@RestController
@RequestMapping("/member_identity")
public class MemberIdentityController implements MemberIdentityApi {

    @Resource(name = "memberIdentityServiceImpl")
    private MemberIdentityService memberIdentityService;

    @Override
    public Result<List<MemberIdentityVo>> selectList(MemberIdentitySelectDto selectDto) {
        return Result.success(memberIdentityService.selectList(selectDto));
    }

    @Override
    public Result<PageData<MemberIdentityVo>> selectPage(PageSelectListDto<MemberIdentitySelectDto> selectDto) {
        return Result.successPage(memberIdentityService.selectPage(selectDto));
    }

    @PostMapping("/save")
    @Operation(summary = "新增或更新会员身份表信息", description = "新增或更新会员身份表基础信息")
    public Result<Long> save(@RequestBody MemberIdentityDto dto) {
        return Result.success(memberIdentityService.save(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除会员身份表记录", description = "根据主键删除会员身份表记录")
    public Result<Integer> delete(@Parameter(description = "会员身份表标识") @PathVariable Long id) {
        return Result.success(memberIdentityService.delete(id));
    }
}