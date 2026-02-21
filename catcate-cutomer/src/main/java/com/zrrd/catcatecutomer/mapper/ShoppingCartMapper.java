package com.zrrd.catcatecutomer.mapper;

import com.zrrd.catcatecutomer.entity.ShoppingCart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 购物车表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {

    /**
     * 根据用户ID获取购物车列表
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<ShoppingCart> selectByUserId(Long userId);
}