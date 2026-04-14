
package com.zrrd.catcatecutomer.service;

import com.alipay.api.AlipayApiException;

import java.math.BigDecimal;

public interface IPaymentService {

    /**
     * 创建支付宝支付订单
     * @param orderNumber 订单编号
     * @param amount 支付金额
     * @param subject 订单标题
     * @return 支付页面HTML
     * @throws AlipayApiException 支付宝API异常
     */
    String createAlipayOrder(String orderNumber, BigDecimal amount, String subject) throws AlipayApiException;

    /**
     * 处理支付宝异步通知
     * @param params 通知参数
     * @return 处理结果
     */
    boolean handleAlipayNotify(java.util.Map<String, String> params);

    /**
     * 查询订单支付状态
     * @param orderNumber 订单编号
     * @return 支付状态
     * @throws AlipayApiException 支付宝API异常
     */
    String queryOrderStatus(String orderNumber) throws AlipayApiException;

    /**
     * 关闭订单
     * @param orderNumber 订单编号
     * @return 关闭结果
     * @throws AlipayApiException 支付宝API异常
     */
    boolean closeOrder(String orderNumber) throws AlipayApiException;

    /**
     * 退款
     * @param orderNumber 订单编号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果
     * @throws AlipayApiException 支付宝API异常
     */
    boolean refund(String orderNumber, BigDecimal refundAmount, String refundReason) throws AlipayApiException;

}