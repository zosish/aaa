package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcatecutomer.entity.Reviews;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 评价表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface IReviewsService extends IService<Reviews> {

    /**
     * 添加评价
     * @param review 评价对象
     * @return 是否成功
     */
    boolean addReview(Reviews review);

    /**
     * 获取商品评价列表（分页）
     * @param productId 商品ID
     * @param current 当前页
     * @param size 每页大小
     * @return 评价分页数据
     */
    Page<Reviews> getProductReviews(Long productId, int current, int size);

    /**
     * 根据订单ID获取评价列表
     * @param orderId 订单ID
     * @return 评价列表
     */
    List<Reviews> getReviewsByOrderId(Long orderId);

    /**
     * 检查用户是否已对订单进行过评价
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否已评价
     */
    boolean userHasReviewedOrder(Long userId, Long orderId);

    /**
     * 获取最新评价列表
     * @param limit 获取数量
     * @return 最新评价列表
     */
    List<Reviews> getLatestReviews(int limit);
}