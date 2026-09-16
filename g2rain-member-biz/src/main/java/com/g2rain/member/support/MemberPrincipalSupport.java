package com.g2rain.member.support;

import com.g2rain.common.enums.SessionType;
import com.g2rain.common.exception.BusinessException;
import com.g2rain.common.exception.SystemErrorCode;
import com.g2rain.common.utils.Asserts;
import com.g2rain.common.web.PrincipalContextHolder;
import com.g2rain.member.dto.MemberIdentitySelectDto;
import com.g2rain.member.dto.MemberSelectDto;
import com.g2rain.member.enums.MemberErrorCode;

import java.util.Objects;

/**
 * MEMBER 会话主体收口：身份只信 {@link PrincipalContextHolder}，对象级校验 organ + member。
 */
public final class MemberPrincipalSupport {

    private MemberPrincipalSupport() {
    }

    public static boolean isMemberSession() {
        return SessionType.isMember(PrincipalContextHolder.getSessionType());
    }

    /**
     * 读取当前可信会员 ID；非 MEMBER 会话或非法 memberId 时抛错。
     */
    public static Long requireMemberId() {
        Asserts.isTrue(isMemberSession(), SystemErrorCode.UNAUTHENTICATED);
        Long memberId = PrincipalContextHolder.getMemberId();
        Asserts.isTrue(memberId != null && memberId > 0L, SystemErrorCode.UNAUTHENTICATED);
        return memberId;
    }

    /**
     * 读取当前可信租户 organId。
     */
    public static Long requireOrganId() {
        Long organId = PrincipalContextHolder.getOrganId();
        Asserts.isTrue(organId != null && organId > 0L, MemberErrorCode.MEMBER_ORGAN_INVALID);
        return organId;
    }

    /**
     * MEMBER 会话查询强制收敛到当前会员，忽略请求中的伪造 id/organId/ids。
     */
    public static void constrainMemberSelect(MemberSelectDto selectDto) {
        if (!isMemberSession() || selectDto == null) {
            return;
        }
        Long memberId = requireMemberId();
        Long organId = requireOrganId();
        selectDto.setId(memberId);
        selectDto.setIds(null);
        selectDto.setOrganId(organId);
    }

    /**
     * MEMBER 会话身份查询强制收敛到当前会员。
     */
    public static void constrainIdentitySelect(MemberIdentitySelectDto selectDto) {
        if (!isMemberSession() || selectDto == null) {
            return;
        }
        selectDto.setMemberId(requireMemberId());
        selectDto.setOrganId(requireOrganId());
        selectDto.setId(null);
        selectDto.setIds(null);
    }

    /**
     * 对象级授权：MEMBER 会话下资源必须同时匹配 Principal 的 organId 与 memberId。
     * 非 MEMBER 会话不在此收紧（仍依赖租户隔离与员工权限模型）。
     */
    public static void assertOwnsMemberResource(Long resourceOrganId, Long resourceMemberId) {
        if (!isMemberSession()) {
            return;
        }
        if (!Objects.equals(requireOrganId(), resourceOrganId)
            || !Objects.equals(requireMemberId(), resourceMemberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCESS_DENIED);
        }
    }

    /**
     * DTO 中若携带 memberId，须与 Principal 一致；否则覆盖为 Principal 值。
     *
     * @return 可信 memberId
     */
    public static Long bindOrRejectDtoMemberId(Long dtoMemberId) {
        Long principalMemberId = requireMemberId();
        if (dtoMemberId != null && dtoMemberId > 0L && !Objects.equals(dtoMemberId, principalMemberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCESS_DENIED);
        }
        return principalMemberId;
    }

    /**
     * DTO 中若携带 organId，须与 Principal 一致；否则覆盖为 Principal 值。
     *
     * @return 可信 organId
     */
    public static Long bindOrRejectDtoOrganId(Long dtoOrganId) {
        Long principalOrganId = requireOrganId();
        if (dtoOrganId != null && dtoOrganId > 0L && !Objects.equals(dtoOrganId, principalOrganId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCESS_DENIED);
        }
        return principalOrganId;
    }
}
