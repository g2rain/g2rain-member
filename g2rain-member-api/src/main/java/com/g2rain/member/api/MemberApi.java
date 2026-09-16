package com.g2rain.member.api;

import com.g2rain.common.model.PageData;
import com.g2rain.common.model.PageSelectListDto;
import com.g2rain.common.model.Result;
import com.g2rain.member.dto.MemberSelectDto;
import com.g2rain.member.vo.MemberVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    /**
     * 查询会员是否允许继续签发/刷新 MEMBER 访问令牌。
     * <p>约定由 {@code g2rain-iam} 在受信服务网络内直连调用（后端调用上下文）。</p>
     *
     * @param organId  可信租户 ID（须与会员归属一致）
     * @param memberId 会员 ID
     * @return 可签发会员资料；不可签发时抛业务错误
     */
    @GetMapping("/active_for_token")
    @Operation(
        summary = "复核会员可签发状态",
        description = "按 organId + memberId 校验会员存在、未删除、状态 NORMAL 且租户一致；供 IAM refresh_token 使用",
        hidden = true
    )
    Result<MemberVo> requireActiveForToken(
        @Parameter(description = "机构 ID") @RequestParam Long organId,
        @Parameter(description = "会员 ID") @RequestParam Long memberId
    );
}
