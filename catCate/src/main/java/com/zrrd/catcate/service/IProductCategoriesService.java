package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.ProductCategories;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 商品分类表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-11-26
 */
public interface IProductCategoriesService extends IService<ProductCategories> {
    Page<ProductCategories> getProductCategoriesList(Map<String, Object> requestParams);
}
