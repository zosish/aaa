package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.Products;
import com.zrrd.catcatecutomer.entity.Reviews;
import com.zrrd.catcatecutomer.mapper.ProductsMapper;
import com.zrrd.catcatecutomer.service.IProductsService;
import com.zrrd.catcatecutomer.service.IReviewsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品信息表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements IProductsService {

    @Resource
    private IReviewsService reviewsService;

    @Override
    public boolean save(Products entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setSalesCount(0); // 新商品销售量初始化为0
        if (entity.getIsAvailable() == null) {
            entity.setIsAvailable((byte) 1); // 默认上架
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(Products entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return super.updateById(entity);
    }

    /**
     * 根据ID获取商品（包含null检查）
     *
     * @param id 商品ID
     * @return 商品对象
     */
    @Override
    public Products getProductById(Long id) {
        if (id == null) {
            return null;
        }
        return this.getById(id);
    }

    /**
     * 分页查询商品列表
     *
     * @param params 查询参数
     * @return 分页结果
     */
    @Override
    public Page<Products> getProductPage(Map<String, Object> params) {
        // 获取分页参数
        int pageNum = (int) params.getOrDefault("pageNum", 1);
        int pageSize = (int) params.getOrDefault("pageSize", 10);

        Page<Products> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        QueryWrapper<Products> queryWrapper = new QueryWrapper<>();

        // 只查询上架商品
        queryWrapper.eq("is_available", 1);

        // 分类筛选
        String category = (String) params.get("category");
        if (StringUtils.hasText(category) && !"all".equals(category)) {
            queryWrapper.eq("category", category);
        }

        // 关键词搜索
        String keyword = (String) params.get("keyword");
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", keyword)
                    .or()
                    .like("description", keyword)
                    .or()
                    .like("brand", keyword));
        }

        // 排序
        String sortBy = (String) params.get("sortBy");
        if ("price_asc".equals(sortBy)) {
            queryWrapper.orderByAsc("price");
        } else if ("price_desc".equals(sortBy)) {
            queryWrapper.orderByDesc("price");
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc("create_time");
        }

        return this.page(page, queryWrapper);
    }

    /**
     * 更新商品库存和销量
     * @param productId 商品ID
     * @param quantity 购买数量（正数表示减少库存增加销量，负数表示增加库存减少销量）
     * @return 是否更新成功
     */
    @Override
    public boolean updateStockAndSales(Long productId, Integer quantity) {
        if (productId == null || quantity == null) {
            return false;
        }

        // 使用乐观锁更新，避免并发问题
        int affectedRows = this.getBaseMapper().updateStockAndSales(
                productId,    // id - 商品ID
                quantity,     // reduceQuantity - 减少的库存数量
                quantity,     // increaseSales - 增加的销量
                quantity      // requiredStock - 需要的库存数量（用于库存检查）
        );

        return affectedRows > 0;
    }

    @Override
    public List<Products> getBestSellingProducts(int limit) {
        // 调用Mapper中的自定义方法获取热销商品
        return this.baseMapper.getBestSellingProducts(limit);
    }
    /**
     * 计算商品平均评分
     */
    private void calculateProductRating(Products product) {
        try {
            // 获取该商品的所有评价
            QueryWrapper<Reviews> reviewQuery = new QueryWrapper<>();
            reviewQuery.eq("target_type", "PRODUCT")
                    .eq("target_id", product.getId())
                    .eq("status", "APPROVED");

            List<Reviews> reviews = reviewsService.list(reviewQuery);

            if (!reviews.isEmpty()) {
                double avgRating = reviews.stream()
                        .mapToInt(Reviews::getRating)
                        .average()
                        .orElse(0.0);

                product.setAverageRating(Math.round(avgRating * 10) / 10.0); // 保留一位小数
            } else {
                product.setAverageRating(0.0);
            }
        } catch (Exception e) {
            product.setAverageRating(0.0);
        }
    }

}

