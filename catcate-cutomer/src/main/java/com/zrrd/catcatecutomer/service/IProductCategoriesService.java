package com.zrrd.catcatecutomer.service;

import com.zrrd.catcatecutomer.entity.ProductCategories;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商品分类表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface IProductCategoriesService extends IService<ProductCategories> {

    /**
     * 获取启用的商品分类列表
     * @return 分类列表
     */
    List<ProductCategories> getActiveCategories();
}