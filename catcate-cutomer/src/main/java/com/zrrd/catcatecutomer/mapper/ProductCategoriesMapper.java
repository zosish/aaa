package com.zrrd.catcatecutomer.mapper;

import com.zrrd.catcatecutomer.entity.ProductCategories;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 商品分类表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Mapper
public interface ProductCategoriesMapper extends BaseMapper<ProductCategories> {

    /**
     * 获取启用的商品分类列表
     * @return 分类列表
     */
    List<ProductCategories> selectActiveCategories();
}