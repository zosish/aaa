package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zrrd.catcatecutomer.entity.Reservations;
import com.zrrd.catcatecutomer.entity.vo.ReservationVO;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 预约记录表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface IReservationsService extends IService<Reservations> {

    /**
     * 根据用户ID获取预约记录（包含猫咪信息）
     * @param userId 用户ID
     * @return 预约记录列表
     */
    List<ReservationVO> getReservationsByUserId(Long userId);

    /**
     * 取消预约
     * @param reservationId 预约ID
     * @return 是否成功
     */
    boolean cancelReservation(Long reservationId);

    /**
     * 检查用户是否在指定日期已有预约
     * @param userId 用户ID
     * @param reservationDate 预约日期
     * @return 是否存在
     */
    boolean hasExistingReservation(Long userId, LocalDate reservationDate);

    /**
     * 检查时间段冲突
     * @param catId 猫咪ID
     * @param reservationDate 预约日期
     * @param timeSlot 时间段
     * @return 是否冲突
     */
    boolean hasTimeSlotConflict(Long catId, LocalDate reservationDate, String timeSlot);

    /**
     * 获取已预订的时间段
     * @param catId 猫咪ID
     * @param reservationDate 预约日期
     * @return 已预订的时间段列表
     */
    List<String> getBookedTimeSlots(Long catId, LocalDate reservationDate);
}