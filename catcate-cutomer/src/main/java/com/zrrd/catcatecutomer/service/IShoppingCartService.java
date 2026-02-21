package com.zrrd.catcatecutomer.service;

import com.zrrd.catcatecutomer.entity.ShoppingCart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 购物车表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface IShoppingCartService extends IService<ShoppingCart> {

    /**
     * 根据用户ID获取购物车列表
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<ShoppingCart> getCartByUserId(Long userId);

    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean addToCart(Long userId, Long productId, Integer quantity);

    /**
     * 更新购物车商品数量
     * @param cartId 购物车项ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean updateCartQuantity(Long cartId, Integer quantity);

    /**
     * 删除购物车项
     * @param cartId 购物车项ID
     * @return 是否成功
     */
    boolean removeCartItem(Long cartId);

    /**
     * 清空用户购物车
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearUserCart(Long userId);
}