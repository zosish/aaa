package com.zrrd.catcatecutomer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcatecutomer.entity.Orders;
import com.zrrd.catcatecutomer.entity.OrderItems;
import com.zrrd.catcatecutomer.service.IOrdersService;
import com.zrrd.catcatecutomer.service.IOrderItemsService;
import com.zrrd.catcatecutomer.service.IProductsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 订单主表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/orders")
public class OrdersController {

    @Resource
    private IOrdersService ordersService;

    @Resource
    private IOrderItemsService orderItemsService;

    @Resource
    private IProductsService productsService;
    /**
     * 生成唯一订单号
     * @return 唯一的订单号字符串
     */
    private String generateUniqueOrderNumber() {
        // 使用更可靠的唯一标识符
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", new java.util.Random().nextInt(10000));
        return "ORDER" + timestamp + random;
    }

    /**
     * 创建订单请求体
     */
    public static class CreateOrderRequest {
        private Long userId;
        private BigDecimal totalAmount;
        private String shippingAddress;
        private String customerNotes;
        private List<OrderItemRequest> items;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public String getCustomerNotes() { return customerNotes; }
        public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    /**
     * 订单商品项请求体
     */
    public static class OrderItemRequest {
        private Long productId;
        private String productName;
        private BigDecimal productPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        private String itemType; // 新增字段：区分商品类型 (PRODUCT/ACTIVITY)

        // getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public BigDecimal getProductPrice() { return productPrice; }
        public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public String getItemType() { return itemType; }
        public void setItemType(String itemType) { this.itemType = itemType; }
    }

    /**
     * 创建订单接口
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 生成唯一订单编号
            String orderNumber = generateUniqueOrderNumber();

            // 检查订单号是否已存在（额外的安全检查）
            Orders existingOrder = ordersService.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Orders>()
                    .eq("order_number", orderNumber));

            if (existingOrder != null) {
                // 如果订单号已存在，重新生成
                orderNumber = generateUniqueOrderNumber();
            }

            // 创建订单主表记录
            Orders order = new Orders();
            order.setOrderNumber(orderNumber);
            order.setUserId(request.getUserId());
            order.setTotalAmount(request.getTotalAmount());
            order.setShippingAddress(request.getShippingAddress());
            order.setCustomerNotes(request.getCustomerNotes());
            order.setPaymentStatus("PENDING"); // 待支付
            order.setPaymentMethod(null); // 支付方式待支付时为空
            order.setPaymentTime(null); // 支付时间待支付时为空
            order.setTransactionId(null); // 交易ID待支付时为空
            order.setOrderStatus("PENDING"); // 待处理
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            // 保存订单
            boolean orderSaved = ordersService.save(order);

            if (orderSaved) {
                // 保存订单商品明细
                List<OrderItems> orderItems = new ArrayList<>();
                for (OrderItemRequest itemRequest : request.getItems()) {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setOrderId(order.getId());
                    orderItem.setProductId(itemRequest.getProductId());
                    orderItem.setProductName(itemRequest.getProductName());
                    orderItem.setProductPrice(itemRequest.getProductPrice());
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setSubtotal(itemRequest.getSubtotal());
                    orderItem.setCreateTime(LocalDateTime.now());
                    orderItems.add(orderItem);
                }

                boolean itemsSaved = orderItemsService.saveBatch(orderItems);

                if (itemsSaved) {
                    // 只对真正的商品更新库存和销量，跳过活动套餐
                    List<Long> failedProductIds = new ArrayList<>();
                    for (OrderItemRequest itemRequest : request.getItems()) {
                        // 如果没有指定itemType或者itemType为PRODUCT，则更新库存
                        // 如果是ACTIVITY类型，则跳过库存更新
                        if (!"ACTIVITY".equals(itemRequest.getItemType())) {
                            boolean updated = productsService.updateStockAndSales(
                                    itemRequest.getProductId(),
                                    itemRequest.getQuantity()
                            );
                            if (!updated) {
                                failedProductIds.add(itemRequest.getProductId());
                            }
                        }
                    }

                    if (!failedProductIds.isEmpty()) {
                        // 如果有商品库存更新失败，删除订单并返回错误
                        ordersService.removeById(order.getId());
                        orderItemsService.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItems>()
                                .eq("order_id", order.getId()));

                        result.put("code", 400);
                        result.put("message", "库存不足，以下商品库存更新失败：" + failedProductIds);
                        return result;
                    }

                    result.put("code", 200);
                    result.put("message", "订单创建成功");
                    Map<String, Object> data = new HashMap<>();
                    data.put("orderId", order.getId());
                    data.put("orderNumber", orderNumber);
                    data.put("totalAmount", request.getTotalAmount());
                    result.put("data", data);
                } else {
                    // 如果商品明细保存失败，删除已创建的订单
                    ordersService.removeById(order.getId());
                    result.put("code", 500);
                    result.put("message", "订单商品明细保存失败");
                }
            } else {
                result.put("code", 500);
                result.put("message", "订单创建失败");
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "创建订单异常：" + e.getMessage());
        }

        return result;
    }



    /**
     * 根据用户ID获取订单列表（分页）
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getOrdersByUserId(@PathVariable Long userId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = new HashMap<>();

        try {
            Page<Orders> pageObj = new Page<>(page, size);
            Page<Orders> ordersPage = ordersService.getOrdersByUserId(userId, pageObj);

            result.put("code", 200);
            result.put("message", "获取成功");
            Map<String, Object> data = new HashMap<>();
            data.put("records", ordersPage.getRecords());
            data.put("total", ordersPage.getTotal());
            data.put("current", ordersPage.getCurrent());
            data.put("size", ordersPage.getSize());
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取订单列表异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 根据订单编号获取订单详情
     */
    @GetMapping("/{orderNumber}")
    public Map<String, Object> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Map<String, Object> result = new HashMap<>();

        try {
            Orders order = ordersService.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Orders>()
                    .eq("order_number", orderNumber));

            if (order != null) {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", order);
            } else {
                result.put("code", 404);
                result.put("message", "订单不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取订单详情异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取订单商品明细
     */
    @GetMapping("/{orderNumber}/items")
    public Map<String, Object> getOrderItems(@PathVariable String orderNumber) {
        Map<String, Object> result = new HashMap<>();

        try {
            Orders order = ordersService.getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Orders>()
                    .eq("order_number", orderNumber));

            if (order != null) {
                List<OrderItems> items = orderItemsService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItems>()
                        .eq("order_id", order.getId()));

                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", items);
            } else {
                result.put("code", 404);
                result.put("message", "订单不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取订单商品明细异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderNumber}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable String orderNumber) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean cancelled = ordersService.cancelOrder(orderNumber);

            if (cancelled) {
                result.put("code", 200);
                result.put("message", "订单取消成功");
                result.put("success", true);
            } else {
                result.put("code", 400);
                result.put("message", "订单取消失败");
                result.put("success", false);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "取消订单异常：" + e.getMessage());
            result.put("success", false);
        }

        return result;
    }

    /**
     * 获取用户最近订单
     */
    @GetMapping("/user/{userId}/recent")
    public Map<String, Object> getUserRecentOrders(@PathVariable Long userId,
                                                   @RequestParam(defaultValue = "5") Integer limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询用户最近的订单，按创建时间倒序排列
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Orders> queryWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("create_time")
                    .last("LIMIT " + limit);

            List<Orders> recentOrders = ordersService.list(queryWrapper);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", recentOrders);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取用户最近订单异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户最近订单（包含订单项）
     */
    @GetMapping("/user/{userId}/recent-with-items")
    public Map<String, Object> getUserRecentOrdersWithItems(@PathVariable Long userId,
                                                            @RequestParam(defaultValue = "5") Integer limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询用户最近的订单，按创建时间倒序排列
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Orders> queryWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("create_time")
                    .last("LIMIT " + limit);

            List<Orders> recentOrders = ordersService.list(queryWrapper);

            // 为每个订单添加订单项信息
            List<Map<String, Object>> ordersWithItems = new ArrayList<>();
            for (Orders order : recentOrders) {
                Map<String, Object> orderWithItems = new HashMap<>();

                // 复制订单基本信息
                orderWithItems.put("id", order.getId());
                orderWithItems.put("orderNumber", order.getOrderNumber());
                orderWithItems.put("userId", order.getUserId());
                orderWithItems.put("totalAmount", order.getTotalAmount());
                orderWithItems.put("orderStatus", order.getOrderStatus());
                orderWithItems.put("paymentStatus", order.getPaymentStatus());
                orderWithItems.put("createTime", order.getCreateTime());
                orderWithItems.put("shippingAddress", order.getShippingAddress());

                // 获取订单项
                List<OrderItems> items = orderItemsService.list(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItems>()
                                .eq("order_id", order.getId())
                );

                // 转换订单项格式以匹配前端期望
                List<Map<String, Object>> formattedItems = new ArrayList<>();
                for (OrderItems item : items) {
                    Map<String, Object> formattedItem = new HashMap<>();
                    formattedItem.put("productImage", "https://picsum.photos/seed/product/100/100"); // 默认图片
                    formattedItem.put("productName", item.getProductName());
                    formattedItem.put("productPrice", item.getProductPrice());
                    formattedItem.put("quantity", item.getQuantity());
                    formattedItems.add(formattedItem);
                }

                orderWithItems.put("items", formattedItems);
                ordersWithItems.add(orderWithItems);
            }

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", ordersWithItems);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取用户最近订单异常：" + e.getMessage());
        }

        return result;
    }
}