package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.ReservationTimeSlots;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 预约时段设置表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-11-25
 */
public interface IReservationTimeSlotsService extends IService<ReservationTimeSlots> {
    Page<ReservationTimeSlots> getReservationTimeSlotsList(Map<String, Object> requestParams);
}
