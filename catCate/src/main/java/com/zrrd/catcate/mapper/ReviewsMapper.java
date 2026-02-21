package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.Reviews;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评价表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Mapper
public interface ReviewsMapper extends BaseMapper<Reviews> {
    List<Reviews> selectReviewsNoNull(Map<String, Object> reviews);
    int countReviewsNoNull(Map<String, Object> params);
}
