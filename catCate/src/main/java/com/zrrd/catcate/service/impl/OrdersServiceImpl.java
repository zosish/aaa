package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Orders;
import com.zrrd.catcate.mapper.OrdersMapper;
import com.zrrd.catcate.service.IOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单主表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Override
    public Page<Orders> getOrderList(Map<String, Object> params) {
        int pageNum = (int) params.getOrDefault("pageNum", 1);
        int pageSize = (int) params.getOrDefault("pageSize", 10);
        String orderNumber = (String) params.get("orderNumber");
        String orderStatus = (String) params.get("orderStatus");
        String paymentStatus = (String) params.get("paymentStatus");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");

        Page<Orders> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();

        if (orderNumber != null && !orderNumber.isEmpty()) {
            queryWrapper.like("order_number", orderNumber);
        }
        if (orderStatus != null && !orderStatus.isEmpty()) {
            queryWrapper.eq("order_status", orderStatus);
        }
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            queryWrapper.eq("payment_status", paymentStatus);
        }
        if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.ge("create_time", startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.le("create_time", endTime);
        }

        queryWrapper.orderByDesc("create_time");
        return this.page(page, queryWrapper);
    }

    @Override
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 待处理订单数
        QueryWrapper<Orders> pendingQw = new QueryWrapper<>();
        pendingQw.eq("order_status", "PENDING");
        long pendingCount = this.count(pendingQw);
        statistics.put("pendingCount", pendingCount);

        // 待支付订单数
        QueryWrapper<Orders> unpaidQw = new QueryWrapper<>();
        unpaidQw.eq("payment_status", "PENDING");
        long unpaidCount = this.count(unpaidQw);
        statistics.put("unpaidCount", unpaidCount);

        // 今日订单数
        QueryWrapper<Orders> todayQw = new QueryWrapper<>();
        todayQw.apply("DATE(create_time) = CURDATE()");
        long todayCount = this.count(todayQw);
        statistics.put("todayCount", todayCount);

        // 本月销售额
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDateTime.now();
        QueryWrapper<Orders> monthSalesQw = new QueryWrapper<>();
        monthSalesQw.eq("payment_status", "PAID")
                .between("payment_time", startOfMonth, endOfMonth);
        List<Map<String, Object>> monthSalesResult = this.getBaseMapper().selectMaps(monthSalesQw);
        BigDecimal monthSales = BigDecimal.ZERO;
        if (!monthSalesResult.isEmpty() && monthSalesResult.get(0).get("total_amount") != null) {
            monthSales = new BigDecimal(monthSalesResult.get(0).get("total_amount").toString());
        }
        statistics.put("monthSales", monthSales);

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getSalesTrend(LocalDate startDate, LocalDate endDate, String groupBy) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(
                        "DATE_FORMAT(payment_time, '%Y-%m-%d') as date",
                        "SUM(total_amount) as sales_amount",
                        "COUNT(*) as order_count"
                )
                .eq("payment_status", "PAID")
                .ge("payment_time", startDate.atStartOfDay())
                .le("payment_time", endDate.atTime(23, 59, 59))
                .groupBy("DATE_FORMAT(payment_time, '%Y-%m-%d')")
                .orderByAsc("date");

        return this.getBaseMapper().selectMaps(queryWrapper);
    }

    @Override
    public BigDecimal getTotalSales(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("SUM(total_amount) as total_sales")
                .eq("payment_status", "PAID")
                .ge("payment_time", startDate.atStartOfDay())
                .le("payment_time", endDate.atTime(23, 59, 59));

        List<Map<String, Object>> result = this.getBaseMapper().selectMaps(queryWrapper);
        if (!result.isEmpty() && result.get(0).get("total_sales") != null) {
            return new BigDecimal(result.get(0).get("total_sales").toString());
        }
        return BigDecimal.ZERO;
    }
}