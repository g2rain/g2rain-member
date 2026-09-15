package com.g2rain.member.controller;

import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.web.PrincipalContextHolder;
import com.g2rain.member.api.MemberApi;
import com.g2rain.member.dto.MemberDto;
import com.g2rain.member.dto.MemberSelectDto;
import com.g2rain.member.service.MemberService;
import com.g2rain.member.vo.MemberVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Override
    public Result<MemberVo> requireActiveForToken(Long organId, Long memberId) {
        // 仅受信后端调用（如 IAM Feign）；经 Gateway 的终端会话不得调用
        Asserts.isTrue(PrincipalContextHolder.isBackEnd(), SystemErrorCode.UNAUTHORIZED);
        return Result.success(memberService.requireActiveForToken(organId, memberId));
    }

    @GetMapping("/current")
    @Operation(
        summary = "当前会员资料",
        description = "MEMBER 会话下从 PrincipalContextHolder 读取 memberId，不接受请求参数伪造身份"
    )
    public Result<MemberVo> current() {
        return Result.success(memberService.getCurrent());
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
