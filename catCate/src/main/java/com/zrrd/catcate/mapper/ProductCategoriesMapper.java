package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.ProductCategories;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品分类表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-11-26
 */
public interface ProductCategoriesMapper extends BaseMapper<ProductCategories> {
    List<ProductCategories> selectProductCategoriesNoNull(Map<String,Object> requestParams);
    int countProductCategoriesNoNull(Map<String,Object> requestParams);
}
