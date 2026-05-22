package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Cats;
import com.zrrd.catcate.entity.Reservations;
import com.zrrd.catcate.entity.Users;
import com.zrrd.catcate.service.IReservationsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 预约记录表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/reservations")
public class ReservationsController {
    @Resource
    private IReservationsService reservationsService;
    /**
     * 获取预约列表（在数据库语句中加入了条件判断，可以按条件查询）
     */
    @PostMapping("/listAppointment")
    public Map<String, Object> listAppointment(@RequestBody Map<String, Object> params) {
        Page<Reservations> page = reservationsService.listAppointment(params);
        Map<String, Object> response = new HashMap<>();
        response.put("total", page.getTotal());
        response.put("list", page.getRecords());
        return response;
    }
    /**
     * 条件查询预约信息（改为使用listAppointment方法，现在这个方法好像无使用，但不确定）
     */
    @PostMapping("/conditionList")
    public List<Reservations> conditionList(@RequestBody(required = false) Reservations reservation) {
//        return reservationsService.conditionList(reservation);
        return null;
    }
    /**
     * 改变预约状态
     */
    @PostMapping("/{id}/status")
    public boolean updateReservationStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String status = params.get("status");
        return reservationsService.updateReservationStatus(id, status);
    }

    /**
     * 保存修改预约信息
     * 用户id为整形，前端传过来的是字符串，无法传过来
     */
    @PostMapping("/addOrUpdateReservation")
    public boolean addOrUpdateReservation(@RequestBody Reservations reservation) {
        System.out.println(reservation);
        
        // 检查时间冲突
        if (hasTimeConflict(reservation)) {
            System.out.println("时间冲突，拒绝预约！");
            return false;
        }
        
        return reservationsService.saveOrUpdate(reservation);
    }
    
    /**
     * 检查时间冲突
     */
    private boolean hasTimeConflict(Reservations newReservation) {
        // 查询该日期下的所有预约（排除已取消的和当前编辑的）
        QueryWrapper<Reservations> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reservation_date", newReservation.getReservationDate());
        queryWrapper.ne("status", "CANCELLED");
        
        // 如果是编辑，排除当前预约
        if (newReservation.getId() != null) {
            queryWrapper.ne("id", newReservation.getId());
        }
        
        List<Reservations> existingReservations = reservationsService.list(queryWrapper);
        
        // 解析新预约的时间段
        String newTimeSlot = newReservation.getTimeSlot();
        if (newTimeSlot == null || !newTimeSlot.contains("-")) {
            return false;
        }
        
        String[] newTimes = newTimeSlot.split("-");
        if (newTimes.length != 2) {
            return false;
        }
        
        Integer newStartMinutes = convertToMinutes(newTimes[0]);
        Integer newEndMinutes = convertToMinutes(newTimes[1]);
        String newTableNumber = newReservation.getTableNumber();
        
        // 检查每个已有预约是否冲突
        for (Reservations existing : existingReservations) {
            String existingTimeSlot = existing.getTimeSlot();
            String existingTableNumber = existing.getTableNumber();
            
            // 如果桌号不同，不冲突
            if (newTableNumber == null || existingTableNumber == null) {
                continue;
            }
            if (!newTableNumber.equals(existingTableNumber)) {
                continue;
            }
            
            // 解析已有预约的时间段
            if (existingTimeSlot == null || !existingTimeSlot.contains("-")) {
                continue;
            }
            
            String[] existingTimes = existingTimeSlot.split("-");
            if (existingTimes.length != 2) {
                continue;
            }
            
            Integer existingStartMinutes = convertToMinutes(existingTimes[0]);
            Integer existingEndMinutes = convertToMinutes(existingTimes[1]);
            
            // 检查时间段是否重叠：新开始 < 已有结束 且 新结束 > 已有开始
            if (newStartMinutes < existingEndMinutes && newEndMinutes > existingStartMinutes) {
                System.out.println("检测到时间冲突：新预约 " + newTimeSlot + 
                    " 桌号 " + newTableNumber + 
                    " 与已有预约 " + existingTimeSlot + 
                    " 桌号 " + existingTableNumber + 
                    " 重叠！");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 将 HH:mm 格式的时间转换为分钟数
     */
    private Integer convertToMinutes(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            if (parts.length != 2) {
                return 0;
            }
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 删除预约信息
     */
    @PostMapping("/deleteReservation")
    public boolean deleteReservation(@RequestBody Long id){
        return reservationsService.removeById(id);
    }

    /**
     * 批量删除
     */
    @PostMapping("/delReservations")
    public boolean delReservations(@RequestBody List<Long> ids){
        return reservationsService.removeByIds(ids);
    }
}
