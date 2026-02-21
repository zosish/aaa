package com.zrrd.catcatecutomer.service;

import com.zrrd.catcatecutomer.entity.OrderItems;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 订单商品明细表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface IOrderItemsService extends IService<OrderItems> {

    /**
     * 根据订单ID获取订单商品明细
     * @param orderId 订单ID
     * @return 订单商品明细列表
     */
    List<OrderItems> getOrderItemsByOrderId(Long orderId);
}