package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcatecutomer.entity.Reviews;
import com.zrrd.catcatecutomer.mapper.ReviewsMapper;
import com.zrrd.catcatecutomer.service.IReviewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 评价表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Service
public class ReviewsServiceImpl extends ServiceImpl<ReviewsMapper, Reviews> implements IReviewsService {

    @Override
    public boolean addReview(Reviews review) {
        // 设置默认值
        review.setStatus("APPROVED"); // 默认审核通过
        review.setLikeCount(0);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());

        return this.save(review);
    }

    @Override
    public Page<Reviews> getProductReviews(Long productId, int current, int size) {
        QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", "PRODUCT")
                .eq("target_id", productId)
                .eq("status", "APPROVED")
                .orderByDesc("create_time");

        Page<Reviews> page = new Page<>(current, size);
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Reviews> getReviewsByOrderId(Long orderId) {
        QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        return this.list(queryWrapper);
    }

    @Override
    public boolean userHasReviewedOrder(Long userId, Long orderId) {
        QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("order_id", orderId);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<Reviews> getLatestReviews(int limit) {
        QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "APPROVED")
                .orderByDesc("create_time")
                .last("LIMIT " + limit);
        return this.list(queryWrapper);
    }
}