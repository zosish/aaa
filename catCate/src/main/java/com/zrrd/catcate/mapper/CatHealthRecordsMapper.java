package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.CatHealthRecords;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 猫咪健康记录表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-11-24
 */
public interface CatHealthRecordsMapper extends BaseMapper<CatHealthRecords> {
    List<CatHealthRecords> selectCatHealthRecordsNoNull(Map<String, Object> requestParams);
    int countCatHealthRecordsNoNull(Map<String, Object> requestParams);
}
