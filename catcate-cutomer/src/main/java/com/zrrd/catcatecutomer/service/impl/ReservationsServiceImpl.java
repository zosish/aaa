package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.Cats;
import com.zrrd.catcatecutomer.entity.Reservations;
import com.zrrd.catcatecutomer.entity.vo.ReservationVO;
import com.zrrd.catcatecutomer.mapper.CatsMapper;
import com.zrrd.catcatecutomer.mapper.ReservationsMapper;
import com.zrrd.catcatecutomer.service.IReservationsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 预约记录表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Service
public class ReservationsServiceImpl extends ServiceImpl<ReservationsMapper, Reservations> implements IReservationsService {

    @Autowired
    private CatsMapper catsMapper;

    @Override
    public List<ReservationVO> getReservationsByUserId(Long userId) {
        QueryWrapper<Reservations> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");

        List<Reservations> reservations = this.list(queryWrapper);

        // 获取所有涉及的猫咪ID
        List<Long> catIds = reservations.stream()
                .map(Reservations::getCatId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询猫咪信息
        Map<Long, Cats> catMap;
        if (!catIds.isEmpty()) {
            List<Cats> cats = catsMapper.selectBatchIds(catIds);
            catMap = cats.stream().collect(Collectors.toMap(Cats::getId, cat -> cat));
        } else {
            catMap = Map.of(); // 空映射
        }

        // 转换为VO对象
        return reservations.stream().map(reservation -> {
            ReservationVO vo = new ReservationVO();
            BeanUtils.copyProperties(reservation, vo);

            // 设置猫咪信息
            Cats cat = catMap.get(reservation.getCatId());
            if (cat != null) {
                vo.setCatName(cat.getName());
                vo.setCatPhoto(cat.getPhotoUrl());
                vo.setCatBreed(cat.getBreed());
                vo.setCatAge(cat.getAge());
                vo.setCatGender(cat.getGender());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean cancelReservation(Long reservationId) {
        UpdateWrapper<Reservations> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", reservationId)
                .set("status", "CANCELLED")
                .set("update_time", LocalDateTime.now());

        return this.update(updateWrapper);
    }

    @Override
    public boolean hasExistingReservation(Long userId, LocalDate reservationDate) {
        QueryWrapper<Reservations> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("reservation_date", reservationDate)
                .ne("status", "CANCELLED");

        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean hasTimeSlotConflict(Long catId, LocalDate reservationDate, String timeSlot) {
        QueryWrapper<Reservations> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cat_id", catId)
                .eq("reservation_date", reservationDate)
                .eq("time_slot", timeSlot)
                .ne("status", "CANCELLED");

        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<String> getBookedTimeSlots(Long catId, LocalDate reservationDate) {
        QueryWrapper<Reservations> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("time_slot")
                .eq("cat_id", catId)
                .eq("reservation_date", reservationDate)
                .ne("status", "CANCELLED");

        List<Reservations> reservations = this.list(queryWrapper);
        return reservations.stream()
                .map(Reservations::getTimeSlot)
                .collect(Collectors.toList());
    }
}