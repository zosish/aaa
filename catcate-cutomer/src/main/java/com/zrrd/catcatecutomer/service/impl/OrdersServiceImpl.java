package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.Orders;
import com.zrrd.catcatecutomer.mapper.OrdersMapper;
import com.zrrd.catcatecutomer.service.IOrdersService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 订单主表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Override
    public Page<Orders> getOrdersByUserId(Long userId, Page<Orders> page) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        return this.page(page, queryWrapper);
    }

    @Override
    public boolean updateOrderByOrderNumber(Orders order) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_number", order.getOrderNumber());
        return this.update(order, queryWrapper);
    }

    @Override
    public boolean updatePaymentStatus(String orderNumber, String paymentStatus, String transactionId, String paymentMethod) {
        // 先查询订单是否存在
        Orders existingOrder = this.getOne(new QueryWrapper<Orders>().eq("order_number", orderNumber));
        if (existingOrder == null) {
            return false;
        }

        // 直接更新指定字段
        Orders updateOrder = new Orders();
        updateOrder.setId(existingOrder.getId());
        updateOrder.setPaymentStatus(paymentStatus);
        updateOrder.setPaymentMethod(paymentMethod);
        updateOrder.setPaymentTime(LocalDateTime.now());
        updateOrder.setTransactionId(transactionId);
        updateOrder.setOrderStatus("PROCESSING".equals(paymentStatus) ? "PROCESSING" : existingOrder.getOrderStatus());
        updateOrder.setUpdateTime(LocalDateTime.now());

        return this.updateById(updateOrder);
    }

    @Override
    public boolean cancelOrder(String orderNumber) {
        Orders order = this.getOne(new QueryWrapper<Orders>().eq("order_number", orderNumber));
        if (order != null && ("PENDING".equals(order.getOrderStatus()) || "PROCESSING".equals(order.getOrderStatus()))) {
            order.setOrderStatus("CANCELLED");
            return this.updateById(order);
        }
        return false;
    }

    @Override
    public Orders getByOrderNumber(String orderNumber) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_number", orderNumber);
        return this.getOne(queryWrapper);
    }

}