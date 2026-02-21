package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Products;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 商品信息表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
public interface IProductsService extends IService<Products> {
    Page<Products> getProductList(Map<String, Object> requestParams);
}
