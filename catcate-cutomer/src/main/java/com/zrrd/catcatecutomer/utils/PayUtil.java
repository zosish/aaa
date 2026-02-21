package com.zrrd.catcatecutomer.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
//import com.second.hand.trading.server.service.OrderService;
import com.zrrd.catcatecutomer.service.IOrdersService;


@Component
public class PayUtil {
    //    @Autowired
//    private OrderService orderService;
    @Autowired
    private IOrdersService orderService;

    //appid
    private final String APP_ID = "9021000158625212";
    //应用私钥
    private final String APP_PRIVATE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmwKCWD01/Zeczs5vQp1/I6b8A1XFvm1kxTcVtbxvcECepNSUckV7ZVcZtNBvkZ3RBPF+kqoQ3yP1D1uutIq0uyDZVdqRN2MbS6X5eYmAh7DrMbdN8uR+DLlXwpKEZLJDvXtVDytbxOcnoxA6TY/V+bAI0wMBIpuImyW2liek8mJBgbknI5vvNQnFZRJyR3uEnDq34QqBH33TWH/K+MtduabKa4rOByaKTWna782BofW6QWdFEEVu9TbnIMd94dE4c6JDpkHZN8HupxEeF/jkxTq5ExlpA7NVdrWRTKZfL8QeLWCgHGnQcOktbBcTTXgnCx4BpL6IGMPmVIi+kk0zWQIDAQAB";

    private final String CHARSET = "UTF-8";
    // 支付宝公钥
    private final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsbGI3FIRyXhP7H7He9zjSsqPOpXsbwfJrnWWBsQOwV+1OuKfHlg7jihWrfvdgugzWx6e7Q6/+ZpSrBagH9p3CPUqHeS70t3aX6x2ALSscI/l3YoFIUjL+cX+ZERYuqqBDgqugqjncZ8wvEI8V4LpxT647wHp6MbQfA2wp5sCFW6QZjSaMMCTgoPDrPb3Luym2gETmT4QmpSdvowTBplsqIOTmLQ7B6rxFGECkQl+rYlp8MC4zVypHjgu42n1mEDV6WVLhqq1eg1dU4LCKI03vHityplrKbxEf2EsMOq9KNq5G2wBZ3deKXQ55wRooZKhqYqGbcw7tbiz406XuFbNRQIDAQAB";
    //这是沙箱接口路径,正式路径为https://openapi.alipay.com/gateway.do
    private final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private final String FORMAT = "JSON";
    //签名方式
    private final String SIGN_TYPE = "RSA2";
    //支付宝异步通知路径,付款完毕后会异步调用本项目的方法,必须为公网地址
    private final String NOTIFY_URL = "http://g22bd859.natappfree.cc/api/alipay/toSuccess";
    //支付宝同步通知路径,也就是当付款完毕后跳转本项目的页面,可以不是公网地址
    private final String RETURN_URL = "http://localhost:8083/api/alipay/toSuccess";
    private AlipayClient alipayClient = null;

    //支付宝官方提供的接口
    public String sendRequestToAlipay(String outTradeNo, Float totalAmount, String subject) throws AlipayApiException {
        //获得初始化的AlipayClient
        alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(RETURN_URL);
        alipayRequest.setNotifyUrl(NOTIFY_URL);

        //商品描述（可空）
        String body = "";
        alipayRequest.setBizContent("{\"out_trade_nos\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + totalAmount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        System.out.println("返回的结果是：" + result);
        return result;
    }

    //    通过订单编号查询
    public String query(String id) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", id);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        String body = null;
        try {
            response = alipayClient.execute(request);
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return body;
    }
}
