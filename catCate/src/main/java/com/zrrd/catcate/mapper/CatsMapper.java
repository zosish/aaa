package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.Cats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 猫咪信息表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Mapper
public interface CatsMapper extends BaseMapper<Cats> {
    List<Cats> selectCatsNoNull(Map<String, Object> requestParams);
    int countCatsNoNull(Map<String, Object> requestParams);
}
