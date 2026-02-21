package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zrrd.catcatecutomer.entity.ReservationTimeSlots;

import java.util.List;

/**
 * <p>
 * 预约时段设置表 服务类
 * </p>
 *
 * @author xyd
 * @since 2026-01-18
 */
public interface IReservationTimeSlotsService extends IService<ReservationTimeSlots> {

    /**
     * 获取启用的预约时段列表
     * @return 启用的预约时段列表
     */
    List<ReservationTimeSlots> getActiveTimeSlots();
}