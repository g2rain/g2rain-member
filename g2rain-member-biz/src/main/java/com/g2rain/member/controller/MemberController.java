package com.g2rain.member.controller;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.member.api.MemberApi;
import com.g2rain.member.dto.MemberDto;
import com.g2rain.member.dto.MemberSelectDto;
import com.g2rain.member.service.MemberService;
import com.g2rain.member.vo.MemberVo;
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
 * 会员表控制器
 * 表名: member
 *
 * @author G2rain Generator
 */
@RestController
@RequestMapping("/member")
public class MemberController implements MemberApi {

    @Resource(name = "memberServiceImpl")
    private MemberService memberService;

    @Override
    public Result<List<MemberVo>> selectList(MemberSelectDto selectDto) {
        return Result.success(memberService.selectList(selectDto));
    }

    @Override
    public Result<PageData<MemberVo>> selectPage(PageSelectListDto<MemberSelectDto> selectDto) {
        return Result.successPage(memberService.selectPage(selectDto));
    }

    @PostMapping("/save")
    @Operation(summary = "新增或更新会员表信息", description = "新增或更新会员表基础信息")
    public Result<Long> save(@RequestBody MemberDto dto) {
        return Result.success(memberService.save(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除会员表记录", description = "根据主键删除会员表记录")
    public Result<Integer> delete(@Parameter(description = "会员表标识") @PathVariable Long id) {
        return Result.success(memberService.delete(id));
    }
}