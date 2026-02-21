package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.Activities;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动信息表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Mapper
public interface ActivitiesMapper extends BaseMapper<Activities> {
    List<Activities> selectActivitiesNoNull(Map<String, Object> params);
    int countActivitiesNoNull(Map<String, Object> params);
}
