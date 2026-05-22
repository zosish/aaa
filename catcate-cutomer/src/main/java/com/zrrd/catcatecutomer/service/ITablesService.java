package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zrrd.catcatecutomer.entity.Tables;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 桌号信息表 服务类
 * </p>
 *
 * @author xyd
 * @since 2026-05-13
 */
public interface ITablesService extends IService<Tables> {

    /**
     * 获取所有可用桌号
     * @return 可用桌号列表
     */
    List<Tables> getAvailableTables();

    /**
     * 根据日期和时间段获取可用桌号
     * @param reservationDate 预约日期
     * @param timeSlot 时间段
     * @return 可用桌号列表
     */
    List<Tables> getAvailableTablesByTime(LocalDate reservationDate, String timeSlot);

    /**
     * 检查桌号是否在指定时间可用
     * @param tableId 桌号ID
     * @param reservationDate 预约日期
     * @param timeSlot 时间段
     * @return 是否可用
     */
    boolean isTableAvailable(Long tableId, LocalDate reservationDate, String timeSlot);

    /**
     * 预约桌号
     * @param tableId 桌号ID
     * @return 是否成功
     */
    boolean reserveTable(Long tableId);

    /**
     * 释放桌号
     * @param tableId 桌号ID
     * @return 是否成功
     */
    boolean releaseTable(Long tableId);
}