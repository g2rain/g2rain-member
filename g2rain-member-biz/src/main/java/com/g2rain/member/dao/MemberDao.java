package com.g2rain.member.dao;

import com.g2rain.member.dao.po.MemberPo;
import com.g2rain.member.dto.MemberSelectDto;
import com.g2rain.data.isolation.annotations.DataIsolation;
import com.g2rain.data.isolation.annotations.IgnoreIsolation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 浼氬憳琛ㄦ暟鎹闂帴鍙? * 琛ㄥ悕: member
 *
 * @author G2rain Generator
 */
@Mapper
@DataIsolation(organIdPropertyName = "organId", organIdColumnName = "organ_id")
public interface MemberDao {

    /**
     * 鎻掑叆鍗曟潯璁板綍
     *
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    int insert(MemberPo entity);

    /**
     * 鎵归噺鎻掑叆璁板綍
     *
     * @param list 瀹炰綋瀵硅薄鍒楄〃
     * @return 褰卞搷琛屾暟
     */
    int insertMultiple(List<MemberPo> list);

    /**
     * 鏍规嵁ID鏇存柊璁板綍
     *
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    int update(MemberPo entity);

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
    int updateByVersion(MemberPo entity);

    /**
     * 鏍规嵁ID鏌ヨ璁板綍
     *
     * @param id 涓婚敭ID
     * @return 瀹炰綋瀵硅薄
     */
    MemberPo selectById(Long id);

    /**
     * 鏍规嵁鏌ヨ鍏ュ弬DTO绛涢€夊垪琛?     *
     * @param selectDto 鏌ヨ鏉′欢DTO
     * @return 瀹炰綋瀵硅薄鍒楄〃
     */
    List<MemberPo> selectList(MemberSelectDto selectDto);

    /**
     * 鎻掑叆鍗曟潯璁板綍锛堝拷鐣ユ暟鎹殧绂伙級
     *
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    @IgnoreIsolation
    int insertWithoutIsolation(MemberPo entity);

    /**
     * 鏍规嵁ID鏇存柊璁板綍锛堝拷鐣ユ暟鎹殧绂伙級
     *
     * @param entity 瀹炰綋瀵硅薄
     * @return 褰卞搷琛屾暟
     */
    @IgnoreIsolation
    int updateWithoutIsolation(MemberPo entity);

    /**
     * 鏍规嵁ID鏌ヨ璁板綍锛堝拷鐣ユ暟鎹殧绂伙級
     *
     * @param id 涓婚敭ID
     * @return 瀹炰綋瀵硅薄
     */
    @IgnoreIsolation
    MemberPo selectByIdWithoutIsolation(Long id);

    /**
     * 鏍规嵁鏌ヨ鍏ュ弬DTO绛涢€夊垪琛紙蹇界暐鏁版嵁闅旂锛?     *
     * @param selectDto 鏌ヨ鏉′欢DTO
     * @return 瀹炰綋瀵硅薄鍒楄〃
     */
    @IgnoreIsolation
    List<MemberPo> selectListWithoutIsolation(MemberSelectDto selectDto);

    /**
     * 根据ID查询记录（包含已逻辑删除，忽略数据隔离）
     *
     * @param id 主键ID
     * @return 实体对象
     */
    @IgnoreIsolation
    MemberPo selectByIdIncludingDeletedWithoutIsolation(Long id);
}
