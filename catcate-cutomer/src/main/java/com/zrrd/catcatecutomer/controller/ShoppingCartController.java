package com.zrrd.catcatecutomer.controller;

import com.zrrd.catcatecutomer.entity.ShoppingCart;
import com.zrrd.catcatecutomer.service.IShoppingCartService;
import com.zrrd.catcatecutomer.utils.JwtHelper;
import com.zrrd.catcatecutomer.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 购物车表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@RestController
@CrossOrigin(value = "*")
public class ShoppingCartController {

    @Autowired
    private IShoppingCartService shoppingCartService;

    @Autowired
    private RedisUtil redisUtil; // 注入Redis工具类

    // 购物车添加请求的数据传输对象
    public static class AddToCartRequest {
        private Long userId;
        private Long productId;
        private Integer quantity = 1;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    /**
     * 从请求头中获取用户ID（使用Redis验证）
     * @param request HTTP请求
     * @return 用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 使用Redis验证token并获取用户ID
            Long userId = redisUtil.getUserIdByToken(token);
            System.out.println("Token验证结果 - Token: " + token + ", UserId: " + userId);
            return userId;
        }
        System.out.println("未找到有效的Authorization头");
        return null;
    }

    /**
     * 获取用户购物车列表
     * @param request HTTP请求
     * @return 购物车列表
     */
    @GetMapping("/catcatecutomer/shopping-cart/list")
    public Map<String, Object> getCartList(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            System.out.println("购物车列表请求 - 用户ID: " + userId);

            if (userId == null) {
                result.put("code", 401);
                result.put("message", "用户未登录");
                return result;
            }

            List<ShoppingCart> cartList = shoppingCartService.getCartByUserId(userId);

            System.out.println("获取到购物车数据: " + cartList.size() + " 条");

            // 丰富购物车数据，包含商品详细信息
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", cartList);
        } catch (Exception e) {
            System.err.println("获取购物车失败: " + e.getMessage());
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "获取失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 添加商品到购物车
     * @param addToCartRequest 添加购物车请求数据
     * @param request HTTP请求
     * @return 结果
     */
    @PostMapping("/catcatecutomer/shopping-cart/add")
    public Map<String, Object> addToCart(@RequestBody AddToCartRequest addToCartRequest,
                                         HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                result.put("code", 401);
                result.put("message", "用户未登录");
                return result;
            }

            Long productId = addToCartRequest.getProductId();
            Integer quantity = addToCartRequest.getQuantity();

            boolean success = shoppingCartService.addToCart(userId, productId, quantity);
            if (success) {
                result.put("code", 200);
                result.put("message", "添加成功");
            } else {
                result.put("code", 400);
                result.put("message", "添加失败，商品不存在或库存不足");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 更新购物车商品数量的数据传输对象
     */
    public static class UpdateCartRequest {
        private Long cartId;
        private Integer quantity;

        // getters and setters
        public Long getCartId() { return cartId; }
        public void setCartId(Long cartId) { this.cartId = cartId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    /**
     * 更新购物车商品数量
     * @param updateRequest 更新请求数据
     * @param request HTTP请求
     * @return 结果
     */
    @PutMapping("/catcatecutomer/shopping-cart/update")
    public Map<String, Object> updateCart(@RequestBody UpdateCartRequest updateRequest,
                                          HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                result.put("code", 401);
                result.put("message", "用户未登录");
                return result;
            }

            Long cartId = updateRequest.getCartId();
            Integer quantity = updateRequest.getQuantity();

            System.out.println("更新购物车 - 用户ID: " + userId + ", 购物车ID: " + cartId + ", 数量: " + quantity);

            boolean success = shoppingCartService.updateCartQuantity(cartId, quantity);
            if (success) {
                result.put("code", 200);
                result.put("message", "更新成功");
            } else {
                result.put("code", 400);
                result.put("message", "更新失败，商品不存在或库存不足");
            }
        } catch (Exception e) {
            System.err.println("更新购物车异常: " + e.getMessage());
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "更新失败：" + e.getMessage());
        }

        return result;
    }


    /**
     * 删除购物车项
     * @param cartId 购物车项ID
     * @param request HTTP请求
     * @return 结果
     */
    @DeleteMapping("/catcatecutomer/shopping-cart/remove/{cartId}")
    public Map<String, Object> removeCartItem(@PathVariable Long cartId,
                                              HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                result.put("code", 401);
                result.put("message", "用户未登录");
                return result;
            }

            boolean success = shoppingCartService.removeCartItem(cartId);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除成功");
            } else {
                result.put("code", 400);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 清空购物车
     * @param request HTTP请求
     * @return 结果
     */
    @DeleteMapping("/catcatecutomer/shopping-cart/clear")
    public Map<String, Object> clearCart(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                result.put("code", 401);
                result.put("message", "用户未登录");
                return result;
            }

            boolean success = shoppingCartService.clearUserCart(userId);
            if (success) {
                result.put("code", 200);
                result.put("message", "清空成功");
            } else {
                result.put("code", 400);
                result.put("message", "清空失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "清空失败：" + e.getMessage());
        }

        return result;
    }
}