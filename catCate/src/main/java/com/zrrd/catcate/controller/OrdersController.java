package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Orders;
import com.zrrd.catcate.entity.OrderItems;
import com.zrrd.catcate.service.IOrdersService;
import com.zrrd.catcate.service.IOrderItemsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单主表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/orders")
public class OrdersController {

    @Resource
    private IOrdersService ordersService;

    @Resource
    private IOrderItemsService orderItemsService;

    /**
     * 分页查询订单列表
     */
    @PostMapping("/list")
    public Map<String, Object> getOrderList(@RequestBody Map<String, Object> params) {
        Page<Orders> page = ordersService.getOrderList(params);
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("list", page.getRecords());
        return result;
    }

    /**
     * 根据ID获取订单详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getOrderDetail(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 获取订单项
        List<OrderItems> orderItems = orderItemsService.getByOrderId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", orderItems);
        return result;
    }

    /**
     * 更新订单状态
     */
    @PostMapping("/updateStatus")
    public boolean updateOrderStatus(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("id").toString());
        String orderStatus = (String) params.get("orderStatus");
        String adminNotes = (String) params.get("adminNotes");

        Orders order = new Orders();
        order.setId(orderId);
        order.setOrderStatus(orderStatus);
        order.setAdminNotes(adminNotes);
        order.setUpdateTime(LocalDateTime.now());

        return ordersService.updateById(order);
    }

    /**
     * 更新支付状态
     */
    @PostMapping("/updatePaymentStatus")
    public boolean updatePaymentStatus(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("id").toString());
        String paymentStatus = (String) params.get("paymentStatus");
        String transactionId = (String) params.get("transactionId");

        Orders order = new Orders();
        order.setId(orderId);
        order.setPaymentStatus(paymentStatus);
        order.setTransactionId(transactionId);
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        return ordersService.updateById(order);
    }

    /**
     * 删除订单
     */
    @PostMapping("/delete/{id}")
    public boolean deleteOrder(@PathVariable Long id) {
        return ordersService.removeById(id);
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getOrderStatistics() {
        return ordersService.getOrderStatistics();
    }

    /**
     * 获取销售趋势数据
     * @param startDate 开始日期 (格式: yyyy-MM-dd)
     * @param endDate 结束日期 (格式: yyyy-MM-dd)
     * @param groupBy 分组方式: day, week, month
     * @return 销售趋势数据
     */
    @GetMapping("/sales/trend")
    public Map<String, Object> getSalesTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "day") String groupBy) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        List<Map<String, Object>> trendData = ordersService.getSalesTrend(start, end, groupBy);
        BigDecimal totalSales = ordersService.getTotalSales(start, end);

        Map<String, Object> result = new HashMap<>();
        result.put("trendData", trendData);
        result.put("totalSales", totalSales);
        result.put("startDate", start);
        result.put("endDate", end);

        return result;
    }
}