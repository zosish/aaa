package com.zrrd.catcatecutomer.controller;

import com.zrrd.catcatecutomer.entity.ReservationTimeSlots;
import com.zrrd.catcatecutomer.service.IReservationTimeSlotsService;
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
 * @since 2026-01-18
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/reservation-time-slots")
public class ReservationTimeSlotsController {

    @Resource
    private IReservationTimeSlotsService reservationTimeSlotsService;

    /**
     * 获取所有可用的预约时段列表
     */
    @GetMapping("/list")
    public Map<String, Object> getAllTimeSlots() {
        try {
            List<ReservationTimeSlots> timeSlots = reservationTimeSlotsService.list();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", timeSlots);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 获取启用的预约时段列表
     */
    @GetMapping("/active")
    public Map<String, Object> getActiveTimeSlots() {
        try {
            List<ReservationTimeSlots> timeSlots = reservationTimeSlotsService.getActiveTimeSlots();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", timeSlots);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 根据ID获取预约时段详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getTimeSlotById(@PathVariable Long id) {
        try {
            ReservationTimeSlots timeSlot = reservationTimeSlotsService.getById(id);
            Map<String, Object> response = new HashMap<>();
            if (timeSlot != null) {
                response.put("code", 200);
                response.put("message", "查询成功");
                response.put("data", timeSlot);
            } else {
                response.put("code", 404);
                response.put("message", "预约时段不存在");
            }
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 分页查询预约时段列表
     */
    @PostMapping("/selectList")
    public Map<String, Object> selectList(@RequestBody Map<String, Object> requestParams) {
        System.out.println("============================" + requestParams);
        try {
            // 这里可以调用服务层的分页查询方法
            List<ReservationTimeSlots> timeSlots = reservationTimeSlotsService.list();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("total", timeSlots.size());
            response.put("records", timeSlots);
            response.put("list", timeSlots);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 添加或修改预约时段
     */
    @PostMapping("/addOrUpdate")
    public Map<String, Object> addOrUpdate(@RequestBody ReservationTimeSlots reservationTimeSlots) {
        try {
            boolean result = reservationTimeSlotsService.saveOrUpdate(reservationTimeSlots);
            Map<String, Object> response = new HashMap<>();
            response.put("code", result ? 200 : 500);
            response.put("message", result ? "操作成功" : "操作失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "操作失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 删除单个预约时段
     */
    @PostMapping("/deleteById")
    public Map<String, Object> deleteById(@RequestBody ReservationTimeSlots reservationTimeSlots) {
        try {
            boolean result = reservationTimeSlotsService.removeById(reservationTimeSlots);
            Map<String, Object> response = new HashMap<>();
            response.put("code", result ? 200 : 500);
            response.put("message", result ? "删除成功" : "删除失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 批量删除预约时段
     */
    @PostMapping("/deleteByIds")
    public Map<String, Object> deleteByIds(@RequestBody List<ReservationTimeSlots> reservationTimeSlots) {
        try {
            boolean result = reservationTimeSlotsService.removeByIds(reservationTimeSlots);
            Map<String, Object> response = new HashMap<>();
            response.put("code", result ? 200 : 500);
            response.put("message", result ? "批量删除成功" : "批量删除失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "批量删除失败：" + e.getMessage());
            return response;
        }
    }
}