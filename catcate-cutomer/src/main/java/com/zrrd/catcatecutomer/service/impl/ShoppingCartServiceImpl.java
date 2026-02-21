package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.Products;
import com.zrrd.catcatecutomer.entity.ShoppingCart;
import com.zrrd.catcatecutomer.mapper.ShoppingCartMapper;
import com.zrrd.catcatecutomer.service.IProductsService;
import com.zrrd.catcatecutomer.service.IShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements IShoppingCartService {

    @Autowired
    private IProductsService productsService;

    @Override
    public List<ShoppingCart> getCartByUserId(Long userId) {
        return this.baseMapper.selectByUserId(userId);
    }

    @Override
    public boolean addToCart(Long userId, Long productId, Integer quantity) {
        // 检查商品是否存在且上架
        Products product = productsService.getById(productId);
        if (product == null || product.getIsAvailable() != 1) {
            return false;
        }

        // 检查库存
        if (product.getStockQuantity() < quantity) {
            return false;
        }

        // 检查是否已存在该商品
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("product_id", productId);
        ShoppingCart existingItem = this.getOne(queryWrapper);

        if (existingItem != null) {
            // 更新数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setUpdateTime(LocalDateTime.now());
            return this.updateById(existingItem);
        } else {
            // 新增购物车项
            ShoppingCart newItem = new ShoppingCart();
            newItem.setUserId(userId);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setCreateTime(LocalDateTime.now());
            newItem.setUpdateTime(LocalDateTime.now());
            return this.save(newItem);
        }
    }

    @Override
    public boolean updateCartQuantity(Long cartId, Integer quantity) {
        ShoppingCart cartItem = this.getById(cartId);
        if (cartItem == null) {
            return false;
        }

        // 检查库存
        Products product = productsService.getById(cartItem.getProductId());
        if (product == null || product.getStockQuantity() < quantity) {
            return false;
        }

        cartItem.setQuantity(quantity);
        cartItem.setUpdateTime(LocalDateTime.now());
        return this.updateById(cartItem);
    }

    @Override
    public boolean removeCartItem(Long cartId) {
        return this.removeById(cartId);
    }

    @Override
    public boolean clearUserCart(Long userId) {
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return this.remove(queryWrapper);
    }
}