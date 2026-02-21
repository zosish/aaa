package com.zrrd.catcatecutomer.controller;

import com.zrrd.catcatecutomer.entity.Reservations;
import com.zrrd.catcatecutomer.entity.vo.ReservationVO;
import com.zrrd.catcatecutomer.service.IReservationsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 预约记录表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/reservations")
public class ReservationsController {

    @Resource
    private IReservationsService reservationsService;

    /**
     * 创建预约
     */
    @PostMapping("/create")
    public Map<String, Object> createReservation(@RequestBody Reservations reservation) {
        try {
            // 验证预约规则
            Map<String, Object> validation = validateReservation(reservation);
            if (!((Boolean) validation.get("valid"))) {
                return validation;
            }

            // 设置创建时间和更新时间
            reservation.setCreateTime(LocalDateTime.now());
            reservation.setUpdateTime(LocalDateTime.now());
            reservation.setStatus("PENDING"); // 默认状态为待确认

            boolean result = reservationsService.save(reservation);

            Map<String, Object> response = new HashMap<>();
            if (result) {
                response.put("code", 200);
                response.put("message", "预约创建成功");
                response.put("data", reservation);
            } else {
                response.put("code", 500);
                response.put("message", "预约创建失败");
            }
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "预约创建异常：" + e.getMessage());
            return response;
        }
    }

    /**
     * 根据用户ID查询预约记录（包含猫咪信息）
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getReservationsByUserId(@PathVariable Long userId) {
        try {
            List<ReservationVO> reservations = reservationsService.getReservationsByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", reservations);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 取消预约
     */
    @PostMapping("/cancel")
    public Map<String, Object> cancelReservation(@RequestBody Map<String, Object> request) {
        try {
            Long reservationId = Long.valueOf(request.get("id").toString());
            boolean result = reservationsService.cancelReservation(reservationId);

            Map<String, Object> response = new HashMap<>();
            response.put("code", result ? 200 : 500);
            response.put("message", result ? "预约取消成功" : "预约取消失败");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "预约取消失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 验证预约规则
     */
    private Map<String, Object> validateReservation(Reservations reservation) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否已存在相同日期的预约
        if (reservationsService.hasExistingReservation(reservation.getUserId(), reservation.getReservationDate())) {
            result.put("code", 400);
            result.put("message", "您在该日期已有预约");
            result.put("valid", false);
            return result;
        }

        // 检查时间段冲突
        if (reservationsService.hasTimeSlotConflict(reservation.getCatId(), reservation.getReservationDate(), reservation.getTimeSlot())) {
            result.put("code", 400);
            result.put("message", "该时间段已被预约");
            result.put("valid", false);
            return result;
        }

        result.put("code", 200);
        result.put("message", "验证通过");
        result.put("valid", true);
        return result;
    }
}