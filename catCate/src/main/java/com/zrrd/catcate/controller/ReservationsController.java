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
        return reservationsService.saveOrUpdate(reservation);
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
