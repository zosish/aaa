package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单主表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
public interface IOrdersService extends IService<Orders> {

    /**
     * 分页查询订单列表
     * @param params 查询参数
     * @return 分页结果
     */
    Page<Orders> getOrderList(Map<String, Object> params);

    /**
     * 获取订单统计信息
     * @return 统计信息
     */
    Map<String, Object> getOrderStatistics();

    /**
     * 获取销售趋势数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param groupBy 分组方式：day(按天), week(按周), month(按月)
     * @return 销售趋势数据
     */
    List<Map<String, Object>> getSalesTrend(LocalDate startDate, LocalDate endDate, String groupBy);

    /**
     * 获取销售额统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总销售额
     */
    BigDecimal getTotalSales(LocalDate startDate, LocalDate endDate);
}