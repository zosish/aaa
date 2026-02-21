
package com.zrrd.catcatecutomer.controller;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zrrd.catcatecutomer.entity.Orders;
import com.zrrd.catcatecutomer.service.IOrdersService;
import com.zrrd.catcatecutomer.service.IPaymentService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catcatecutomer/payment")
@CrossOrigin("*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Resource
    private IPaymentService paymentService;

    @Resource
    private IOrdersService ordersService;

    @PostMapping("/alipay/create")
    public String createAlipayPaymentJson(@RequestBody Map<String, Object> requestData) {
        try {
            String orderNumber = (String) requestData.get("orderNumber");
            BigDecimal amount = new BigDecimal(requestData.get("amount").toString());
            String subject = (String) requestData.get("subject");

            logger.info("发起支付宝支付 - 订单号: {}, 金额: {}, 标题: {}", orderNumber, amount, subject);

            // 检查订单是否存在且状态为待支付
            Orders order = ordersService.getOne(new QueryWrapper<Orders>().eq("order_number", orderNumber));
            if (order == null) {
                logger.error("订单不存在: {}", orderNumber);
                return "<html><body><h1>订单不存在</h1><p>订单号: " + orderNumber + "</p><p>请检查订单是否创建成功</p></body></html>";
            }

            if (!"PENDING".equals(order.getPaymentStatus())) {
                logger.warn("订单状态不正确 - 订单号: {}, 当前状态: {}", orderNumber, order.getPaymentStatus());
                return "<html><body><h1>订单状态不正确</h1><p>订单号: " + orderNumber + "</p><p>当前状态: " + order.getPaymentStatus() + "</p><p>只有待支付状态的订单才能发起支付</p></body></html>";
            }

            String result = paymentService.createAlipayOrder(orderNumber, amount, subject);

            logger.info("支付宝返回结果类型: {}, 内容长度: {}", result.getClass().getSimpleName(), result.length());
            logger.debug("支付宝返回结果预览: {}", result.substring(0, Math.min(200, result.length())));

            // 确保返回的是有效的HTML表单
            if (result != null && result.contains("<form")) {
                return result;
            } else {
                logger.error("支付宝返回结果格式不正确: {}", result);
                return "<html><body><h1>支付服务异常</h1><p>未能生成有效的支付表单</p><p>请稍后重试或联系客服</p></body></html>";
            }
        } catch (Exception e) {
            logger.error("创建支付宝订单失败", e);
            return "<html><body><h1>支付请求失败</h1><p>错误信息: " + e.getMessage() + "</p><p>请稍后重试</p></body></html>";
        }
    }

    /**
     * 发起支付宝支付 - 表单格式请求（备用接口）
     */
    @PostMapping("/alipay/create/form")
    public String createAlipayPayment(@RequestParam String orderNumber,
                                      @RequestParam BigDecimal amount,
                                      @RequestParam String subject) {
        try {
            logger.info("发起支付宝支付(表单) - 订单号: {}, 金额: {}, 标题: {}", orderNumber, amount, subject);
            return paymentService.createAlipayOrder(orderNumber, amount, subject);
        } catch (AlipayApiException e) {
            logger.error("创建支付宝订单失败", e);
            return "<html><body><h1>支付请求失败</h1><p>" + e.getMessage() + "</p></body></html>";
        }
    }

    /**
     * 支付宝同步回调接口 - 更新订单状态
     */
    @GetMapping("/alipay/return")
    public String alipayReturn(HttpServletRequest request) {
        logger.info("收到支付宝同步回调");

        try {
            // 获取回调参数
            String outTradeNo = request.getParameter("out_trade_no"); // 商户订单号
            String tradeNo = request.getParameter("trade_no"); // 支付宝交易号
            String totalAmount = request.getParameter("total_amount"); // 交易金额

            logger.info("支付成功回调 - 订单号: {}, 交易号: {}, 金额: {}", outTradeNo, tradeNo, totalAmount);

            // 验证订单是否存在
            Orders existingOrder = ordersService.getOne(new QueryWrapper<Orders>().eq("order_number", outTradeNo));
            if (existingOrder == null) {
                logger.error("订单不存在: {}", outTradeNo);
                return "<html><body><h1>订单不存在</h1><p>订单号: " + outTradeNo + "</p></body></html>";
            }

            // 检查订单状态，如果已经是已支付状态则直接跳转
            if ("PAID".equals(existingOrder.getPaymentStatus())) {
                logger.info("订单已支付，直接跳转: {}", outTradeNo);
                return "<html><head>" +
                        "<meta http-equiv='refresh' content='0;url=http://localhost:8080/PaymentSuccessfulPage?orderNumber=" + outTradeNo + "'>" +
                        "</head>" +
                        "<body>" +
                        "<h1>支付成功！</h1>" +
                        "<p>订单号: " + outTradeNo + "</p>" +
                        "<p>正在跳转到支付成功页面...</p>" +
                        "</body></html>";
            }

            // 更新订单状态为已支付
            Orders order = new Orders();
            order.setOrderNumber(outTradeNo);
            order.setPaymentStatus("PAID");
            order.setPaymentMethod("ALIPAY");
            order.setPaymentTime(LocalDateTime.now());
            order.setTransactionId(tradeNo);
            order.setOrderStatus("PROCESSING"); // 支付成功后改为处理中
            order.setUpdateTime(LocalDateTime.now());

            boolean updated = ordersService.updateOrderByOrderNumber(order);

            if (updated) {
                logger.info("订单状态更新成功: {}", outTradeNo);
                // 立即跳转到支付成功页面
                return "<html><head>" +
                        "<meta http-equiv='refresh' content='0;url=http://localhost:8080/PaymentSuccessfulPage?orderNumber=" + outTradeNo + "'>" +
                        "</head>" +
                        "<body>" +
                        "<h1>支付成功！</h1>" +
                        "<p>订单号: " + outTradeNo + "</p>" +
                        "<p>正在跳转到支付成功页面...</p>" +
                        "</body></html>";
            } else {
                logger.error("订单状态更新失败: {}", outTradeNo);
                return "<html><body><h1>支付成功但订单更新失败</h1><p>请联系客服处理</p></body></html>";
            }
        } catch (Exception e) {
            logger.error("处理支付宝同步回调异常", e);
            return "<html><body><h1>支付回调处理失败</h1><p>" + e.getMessage() + "</p></body></html>";
        }
    }

    @PostMapping("/alipay/notify")
    public String handleAlipayNotify(HttpServletRequest request) {
        logger.info("收到支付宝异步通知");

        try {
            // 获取支付宝POST过来反馈信息
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                String name = entry.getKey();
                String[] values = entry.getValue();
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            logger.info("异步通知参数: {}", params);

            // 验证签名并处理通知
            boolean handleResult = paymentService.handleAlipayNotify(params);

            // 返回支付宝要求的响应
            if (handleResult) {
                logger.info("支付宝异步通知处理成功");
                return "success";
            } else {
                logger.warn("支付宝异步通知处理失败");
                return "fail";
            }
        } catch (Exception e) {
            logger.error("处理支付宝异步通知异常", e);
            return "fail";
        }
    }

    private String processJsonNotify(Map<String, String> params) {
        boolean isValid = paymentService.handleAlipayNotify(params);
        if (!isValid) {
            return "fail";
        }

        String orderNumber = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            Orders order = ordersService.getByOrderNumber(orderNumber);
            if (order != null && "PENDING".equals(order.getPaymentStatus())) {
                order.setPaymentStatus("PAID");
                order.setPaymentTime(LocalDateTime.now());
                order.setTransactionId(params.get("trade_no"));
                ordersService.updateById(order);
            }
        }

        return "success";
    }

    private String processFormNotify(HttpServletRequest request, HttpServletResponse response) {
        // 读取 form-data 或 query 参数
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()[0]
                ));

        return processJsonNotify(params);
    }
    /**
     * 查询订单支付状态
     */
    @GetMapping("/order/{orderNumber}/status")
    public String queryOrderStatus(@PathVariable String orderNumber) {
        try {
            String status = paymentService.queryOrderStatus(orderNumber);
            return "{\"status\":\"" + status + "\"}";
        } catch (AlipayApiException e) {
            logger.error("查询订单状态失败", e);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 关闭订单
     */
    @PostMapping("/order/{orderNumber}/close")
    public String closeOrder(@PathVariable String orderNumber) {
        try {
            boolean result = paymentService.closeOrder(orderNumber);
            return "{\"success\":" + result + "}";
        } catch (AlipayApiException e) {
            logger.error("关闭订单失败", e);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}