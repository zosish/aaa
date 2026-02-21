package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcatecutomer.entity.Products;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品信息表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface IProductsService extends IService<Products> {

    /**
     * 分页查询商品列表
     * @param params 查询参数
     * @return 分页结果
     */
    Page<Products> getProductPage(Map<String, Object> params);

    /**
     * 根据ID获取商品（包含null检查）
     * @param id 商品ID
     * @return 商品对象
     */
    Products getProductById(Long id);

    /**
     * 更新商品库存和销量
     * @param productId 商品ID
     * @param quantity 购买数量（正数表示减少库存增加销量，负数表示增加库存减少销量）
     * @return 是否更新成功
     */
    boolean updateStockAndSales(Long productId, Integer quantity);

    /**
     * 获取销售数量最多的热门商品
     * @param limit 返回数量，默认4个
     * @return 热门商品列表
     */
    List<Products> getBestSellingProducts(int limit);
}