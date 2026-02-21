
package com.zrrd.catcatecutomer.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.zrrd.catcatecutomer.config.AlipayConfig;
import com.zrrd.catcatecutomer.entity.Orders;
import com.zrrd.catcatecutomer.service.IOrdersService;
import com.zrrd.catcatecutomer.service.IPaymentService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PaymentServiceImpl implements IPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private IOrdersService ordersService;

    @Override
    public String createAlipayOrder(String orderNumber, BigDecimal amount, String subject) throws AlipayApiException {
        // 创建AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getMerchantPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
        );

        // 创建API对应的request
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(alipayConfig.getReturnUrl());
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        // 填充业务参数
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + orderNumber + "\"," +
                "\"total_amount\":\"" + amount + "\"," +
                "\"subject\":\"" + subject + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"" +
                "}");

        // 执行请求
        String result = alipayClient.pageExecute(request).getBody();
        logger.info("支付宝支付请求结果: {}", result);
        return result;
    }

    @Override
    public boolean handleAlipayNotify(Map<String, String> params) {
        try {
            // 验证签名
            boolean verifyResult = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (!verifyResult) {
                logger.warn("支付宝异步通知签名验证失败");
                return false;
            }

            // 获取订单信息
            String outTradeNo = params.get("out_trade_no"); // 商户订单号
            String tradeNo = params.get("trade_no"); // 支付宝交易号
            String tradeStatus = params.get("trade_status"); // 交易状态

            logger.info("收到支付宝异步通知 - 订单号: {}, 交易号: {}, 状态: {}", outTradeNo, tradeNo, tradeStatus);

            // 根据交易状态处理业务逻辑
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功的处理逻辑
                boolean updateResult = ordersService.updatePaymentStatus(outTradeNo, "PAID", tradeNo, "ALIPAY");

                if (updateResult) {
                    logger.info("订单支付状态更新成功: {}", outTradeNo);
                    // 可以在这里添加其他业务逻辑，比如发送通知等
                    return true;
                } else {
                    logger.error("订单支付状态更新失败: {}", outTradeNo);
                    return false;
                }
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                // 交易关闭的处理逻辑
                Orders order = new Orders();
                order.setOrderNumber(outTradeNo);
                order.setPaymentStatus("FAILED");
                order.setOrderStatus("CANCELLED");
                order.setUpdateTime(LocalDateTime.now());

                boolean updateResult = ordersService.updateOrderByOrderNumber(order);
                logger.info("订单已关闭: {}", outTradeNo);
                return updateResult;
            }

            return true;
        } catch (Exception e) {
            logger.error("处理支付宝异步通知异常", e);
            return false;
        }
    }

    @Override
    public String queryOrderStatus(String orderNumber) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getMerchantPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
        );

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + orderNumber + "\"" +
                "}");

        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            return response.getTradeStatus();
        } else {
            throw new AlipayApiException("查询订单状态失败: " + response.getMsg());
        }
    }

    @Override
    public boolean closeOrder(String orderNumber) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getMerchantPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
        );

        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + orderNumber + "\"" +
                "}");

        AlipayTradeCloseResponse response = alipayClient.execute(request);
        return response.isSuccess();
    }
}