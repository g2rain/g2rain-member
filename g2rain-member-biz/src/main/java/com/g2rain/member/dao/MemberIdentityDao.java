package com.g2rain.member.dao;

import com.g2rain.member.dao.po.MemberIdentityPo;
import com.g2rain.member.dto.MemberIdentitySelectDto;
import com.g2rain.data.isolation.annotations.DataIsolation;
import com.g2rain.data.isolation.annotations.IgnoreIsolation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 浼氬憳韬唤琛ㄦ暟鎹闂帴鍙? * 琛ㄥ悕: member_identity
 *
 * @author G2rain Generator
 */
@Mapper
@DataIsolation(organIdPropertyName = "organId", organIdColumnName = "organ_id")
public interface MemberIdentityDao {

    /**
     * 鎻掑叆鍗曟潯璁板綍
     *
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    int insert(MemberIdentityPo entity);

    /**
     * 鎵归噺鎻掑叆璁板綍
     *
     * @param list 瀹炰綋瀵硅薄鍒楄〃
     * @return 褰卞搷琛屾暟
     */
    int insertMultiple(List<MemberIdentityPo> list);

    /**
     * 鏍规嵁ID鏇存柊璁板綍
     *
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    int update(MemberIdentityPo entity);

    /**
     * 鏍规嵁ID鍒犻櫎璁板綍
     *
     * @param id 涓婚敭ID
     * @return 褰卞搷琛屾暟
     */
    int delete(Long id);

    /**
     * 鏍规嵁ID鍜孷ersion鏇存柊璁板綍锛堜箰瑙傞攣鏇存柊锛?     *
     * @param entity 瀹炰綋瀵硅薄锛堝繀椤诲寘鍚玽ersion瀛楁锛?     * @return 褰卞搷琛屾暟
     */
    int updateByVersion(MemberIdentityPo entity);

    /**
     * 鏍规嵁ID鏌ヨ璁板綍
     *
     * @param id 涓婚敭ID
     * @return 瀹炰綋瀵硅薄
     */
    MemberIdentityPo selectById(Long id);

    /**
     * 鏍规嵁鏌ヨ鍏ュ弬DTO绛涢€夊垪琛?     *
     * @param selectDto 鏌ヨ鏉′欢DTO
     * @return 瀹炰綋瀵硅薄鍒楄〃
     */
    List<MemberIdentityPo> selectList(MemberIdentitySelectDto selectDto);

    /**
     * 鎻掑叆鍗曟潯璁板綍锛堝拷鐣ユ暟鎹殧绂伙級
     *
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    @IgnoreIsolation
    int insertWithoutIsolation(MemberIdentityPo entity);

    /**
     * 鏍规嵁ID鏇存柊璁板綍锛堝拷鐣ユ暟鎹殧绂伙級
     *
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    @IgnoreIsolation
    int updateWithoutIsolation(MemberIdentityPo entity);

    /**
     * 鏍规嵁ID鏌ヨ璁板綍锛堝拷鐣ユ暟鎹殧绂伙級
     *
     * @param id 涓婚敭ID
     * @return 瀹炰綋瀵硅薄
     */
    @IgnoreIsolation
    MemberIdentityPo selectByIdWithoutIsolation(Long id);

    /**
     * 鏍规嵁鏌ヨ鍏ュ弬DTO绛涢€夊垪琛紙蹇界暐鏁版嵁闅旂锛?     *
     * @param selectDto 鏌ヨ鏉′欢DTO
     * @return 瀹炰綋瀵硅薄鍒楄〃
     */
    @IgnoreIsolation
    List<MemberIdentityPo> selectListWithoutIsolation(MemberIdentitySelectDto selectDto);

    /**
     * 按租户身份键查询（包含已逻辑删除，忽略数据隔离）
     */
    @IgnoreIsolation
    MemberIdentityPo selectByOrganIdentityKeyIncludingDeletedWithoutIsolation(
        @Param("organId") Long organId,
        @Param("identityType") String identityType,
        @Param("identityValue") String identityValue
    );
}
