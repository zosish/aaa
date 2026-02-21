package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.ReservationTimeSlots;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 预约时段设置表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-11-25
 */
public interface ReservationTimeSlotsMapper extends BaseMapper<ReservationTimeSlots> {
    List<ReservationTimeSlots> selectReservationTimeSlotsNoNull(Map<String, Object> params);
    int countReservationTimeSlotsNoNull(Map<String, Object> params);
}
