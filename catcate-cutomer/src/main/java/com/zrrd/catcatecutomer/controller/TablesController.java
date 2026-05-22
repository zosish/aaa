package com.zrrd.catcatecutomer.controller;

import com.zrrd.catcatecutomer.entity.Tables;
import com.zrrd.catcatecutomer.service.ITablesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 桌号信息表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2026-05-13
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/tables")
public class TablesController {

    @Resource
    private ITablesService tablesService;

    /**
     * 获取所有桌号
     */
    @GetMapping("/list")
    public Map<String, Object> getAllTables() {
        try {
            List<Tables> tables = tablesService.list();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", tables);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 获取可用桌号
     */
    @GetMapping("/available")
    public Map<String, Object> getAvailableTables() {
        try {
            List<Tables> tables = tablesService.getAvailableTables();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", tables);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 根据日期和时间段获取可用桌号
     * @param reservationDate 预约日期
     * @param timeSlot 时间段
     */
    @GetMapping("/available/by-time")
    public Map<String, Object> getAvailableTablesByTime(
            @RequestParam String reservationDate,
            @RequestParam String timeSlot) {
        try {
            LocalDate date = LocalDate.parse(reservationDate);
            List<Tables> tables = tablesService.getAvailableTablesByTime(date, timeSlot);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", tables);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 检查桌号是否可用
     */
    @GetMapping("/check-availability")
    public Map<String, Object> checkTableAvailability(
            @RequestParam Long tableId,
            @RequestParam String reservationDate,
            @RequestParam String timeSlot) {
        try {
            LocalDate date = LocalDate.parse(reservationDate);
            boolean available = tablesService.isTableAvailable(tableId, date, timeSlot);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", available ? "桌号可用" : "桌号已被预约");
            response.put("data", available);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "检查失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 预约桌号
     */
    @PostMapping("/reserve")
    public Map<String, Object> reserveTable(@RequestBody Map<String, Object> request) {
        try {
            Long tableId = Long.valueOf(request.get("tableId").toString());
            boolean result = tablesService.reserveTable(tableId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", result ? 200 : 500);
            response.put("message", result ? "桌号预约成功" : "桌号预约失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "预约失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 释放桌号
     */
    @PostMapping("/release")
    public Map<String, Object> releaseTable(@RequestBody Map<String, Object> request) {
        try {
            Long tableId = Long.valueOf(request.get("tableId").toString());
            boolean result = tablesService.releaseTable(tableId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", result ? 200 : 500);
            response.put("message", result ? "桌号释放成功" : "桌号释放失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "释放失败：" + e.getMessage());
            return response;
        }
    }
}