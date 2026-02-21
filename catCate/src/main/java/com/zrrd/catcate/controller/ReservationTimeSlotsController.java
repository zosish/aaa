package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.ReservationTimeSlots;
import com.zrrd.catcate.service.IReservationTimeSlotsService;
import com.zrrd.catcate.service.impl.ReservationTimeSlotsServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 预约时段设置表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-11-25
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/reservationTimeSlots")
public class ReservationTimeSlotsController {
    @Resource
    private IReservationTimeSlotsService reservationTimeSlotsService;

    /**
     * 分页查询预约时段列表
     */
    @PostMapping("/selectList")
    public Map<String, Object> selectList(@RequestBody Map<String, Object> requestParams) {
        System.out.println("============================" + requestParams);
        // 执行分页查询
        Page<ReservationTimeSlots> page = reservationTimeSlotsService.getReservationTimeSlotsList(requestParams);
        Map<String, Object> response = new HashMap<>();
        response.put("total", page.getTotal());
        response.put("list", page.getRecords());
        return response;
    }

    /**
     * 添加或修改预约时段
     */
    @PostMapping("/addOrUpdate")
    public boolean addORUpdate(@RequestBody ReservationTimeSlots reservationTimeSlots){
        return reservationTimeSlotsService.saveOrUpdate(reservationTimeSlots);
    }

    /**
     * 删除单个预约时段
     */
    @PostMapping("/deleteById")
    public boolean deleteById(@RequestBody ReservationTimeSlots reservationTimeSlots){
        return reservationTimeSlotsService.removeById(reservationTimeSlots);
    }

    /**
     * 批量删除预约时段
     */
    @PostMapping("/deleteByIds")
    public boolean deleteByIds(@RequestBody List<ReservationTimeSlots> reservationTimeSlots){
        return reservationTimeSlotsService.removeByIds(reservationTimeSlots);
    }
}
