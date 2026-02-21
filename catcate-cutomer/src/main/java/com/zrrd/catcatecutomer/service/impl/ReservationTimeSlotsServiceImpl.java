package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.ReservationTimeSlots;
import com.zrrd.catcatecutomer.mapper.ReservationTimeSlotsMapper;
import com.zrrd.catcatecutomer.service.IReservationTimeSlotsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 预约时段设置表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2026-01-18
 */
@Service
public class ReservationTimeSlotsServiceImpl extends ServiceImpl<ReservationTimeSlotsMapper, ReservationTimeSlots> implements IReservationTimeSlotsService {

    @Override
    public List<ReservationTimeSlots> getActiveTimeSlots() {
        QueryWrapper<ReservationTimeSlots> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", (byte) 1)
                .orderByAsc("sort_order");

        return this.list(queryWrapper);
    }
}