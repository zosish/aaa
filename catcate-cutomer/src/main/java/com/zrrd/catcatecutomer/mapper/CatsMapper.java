package com.zrrd.catcatecutomer.mapper;

import com.zrrd.catcatecutomer.entity.Cats;
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
 * @since 2025-10-29
 */
@Mapper
public interface CatsMapper extends BaseMapper<Cats> {
    List<Cats> selectCatsNoNull(Map<String, Object> requestParams);
    int countCatsNoNull(Map<String, Object> requestParams);
    /**
     * 获取预约次数最多的热门猫咪
     * @param limit 返回数量
     * @return 热门猫咪列表
     */
    List<Cats> getPopularCats(int limit);
}
