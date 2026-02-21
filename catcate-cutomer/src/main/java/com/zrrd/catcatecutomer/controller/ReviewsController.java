package com.zrrd.catcatecutomer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcatecutomer.entity.Orders;
import com.zrrd.catcatecutomer.entity.Reviews;
import com.zrrd.catcatecutomer.service.IOrdersService;
import com.zrrd.catcatecutomer.service.IReviewsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评价表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/reviews")
public class ReviewsController {

    @Resource
    private IReviewsService reviewsService;

    @Resource
    private IOrdersService ordersService;

    /**
     * 添加评价
     */
    @PostMapping("/add")
    public Map<String, Object> addReview(@RequestBody Reviews review) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证订单状态
            Orders order = ordersService.getById(review.getOrderId());
            if (order == null) {
                result.put("code", 400);
                result.put("message", "订单不存在");
                return result;
            }

            if (!"PAID".equals(order.getPaymentStatus()) && !"COMPLETED".equals(order.getOrderStatus())) {
                result.put("code", 400);
                result.put("message", "只有已完成支付的订单才能进行评价");
                return result;
            }

            // 检查是否已评价
            if (reviewsService.userHasReviewedOrder(review.getUserId(), review.getOrderId())) {
                result.put("code", 400);
                result.put("message", "该订单已评价");
                return result;
            }

            // 添加评价
            boolean success = reviewsService.addReview(review);
            if (success) {
                result.put("code", 200);
                result.put("message", "评价成功");
            } else {
                result.put("code", 500);
                result.put("message", "评价失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "系统异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取商品评价列表（分页）
     */
    @GetMapping("/product/{productId}")
    public Map<String, Object> getProductReviews(@PathVariable Long productId,
                                                 @RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = new HashMap<>();

        try {
            Page<Reviews> page = reviewsService.getProductReviews(productId, current, size);
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", page);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 根据订单ID获取评价
     */
    @GetMapping("/order/{orderId}")
    public Map<String, Object> getReviewsByOrderId(@PathVariable Long orderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Reviews> reviews = reviewsService.getReviewsByOrderId(orderId);
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", reviews);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 检查订单是否已评价
     */
    @GetMapping("/check/{userId}/{orderId}")
    public Map<String, Object> checkOrderReviewed(@PathVariable Long userId, @PathVariable Long orderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean hasReviewed = reviewsService.userHasReviewedOrder(userId, orderId);
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", hasReviewed);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取最新评价
     */
    @GetMapping("/latest")
    public Map<String, Object> getLatestReviews(@RequestParam(defaultValue = "6") Integer limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Reviews> reviews = reviewsService.getLatestReviews(limit);
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", reviews);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户评价列表
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserReviews(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询用户的所有评价，按创建时间倒序排列
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Reviews> queryWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("create_time");

            List<Reviews> userReviews = reviewsService.list(queryWrapper);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", userReviews);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取用户评价异常：" + e.getMessage());
        }

        return result;
    }
}