package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zrrd.catcatecutomer.entity.Orders;

import java.util.List;

/**
 * <p>
 * 订单主表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface IOrdersService extends IService<Orders> {

    /**
     * 根据用户ID获取订单列表（分页）
     * @param userId 用户ID
     * @param page 分页对象
     * @return 订单分页结果
     */
    Page<Orders> getOrdersByUserId(Long userId, Page<Orders> page);

    /**
     * 根据订单编号更新订单
     * @param order 订单信息
     * @return 是否更新成功
     */
    boolean updateOrderByOrderNumber(Orders order);

    /**
     * 根据订单编号更新订单支付状态
     * @param orderNumber 订单编号
     * @param paymentStatus 支付状态
     * @param transactionId 交易ID
     * @param paymentMethod 支付方式
     * @return 是否更新成功
     */
    boolean updatePaymentStatus(String orderNumber, String paymentStatus, String transactionId, String paymentMethod);

    /**
     * 取消订单
     * @param orderNumber 订单编号
     * @return 是否取消成功
     */
    boolean cancelOrder(String orderNumber);

    /**
     * 根据订单号获取订单
     * @param orderNumber 订单编号
     * @return 订单对象
     */
    Orders getByOrderNumber(String orderNumber);
}