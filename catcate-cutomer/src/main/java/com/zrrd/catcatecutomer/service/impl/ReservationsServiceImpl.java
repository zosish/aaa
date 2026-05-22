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
        System.out.println("=== 获取用户预约列表 ===");
        System.out.println("用户ID: " + userId);
        
        try {
            if (userId == null) {
                System.err.println("用户ID为空");
                return List.of();
            }
            
            QueryWrapper<Reservations> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("create_time");

            List<Reservations> reservations = this.list(queryWrapper);
            
            System.out.println("查询到预约数量: " + (reservations != null ? reservations.size() : 0));
            
            if (reservations == null || reservations.isEmpty()) {
                System.out.println("没有预约记录，返回空列表");
                return List.of();
            }

            List<Long> catIds = reservations.stream()
                    .map(Reservations::getCatId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            System.out.println("涉及的猫咪ID: " + catIds);

            Map<Long, Cats> catMap = buildCatMap(catIds);

            List<ReservationVO> result = reservations.stream()
                    .filter(reservation -> reservation != null)
                    .map(reservation -> {
                        try {
                            ReservationVO vo = new ReservationVO();
                            BeanUtils.copyProperties(reservation, vo);

                            Cats cat = catMap.get(reservation.getCatId());
                            if (cat != null) {
                                vo.setCatName(cat.getName());
                                vo.setCatPhoto(cat.getPhotoUrl());
                                vo.setCatBreed(cat.getBreed());
                                vo.setCatAge(cat.getAge());
                                vo.setCatGender(cat.getGender());
                            } else {
                                System.out.println("未找到猫咪信息，catId: " + reservation.getCatId());
                            }

                            return vo;
                        } catch (Exception e) {
                            System.err.println("转换预约记录失败: " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(vo -> vo != null)
                    .collect(Collectors.toList());
            
            System.out.println("转换完成，返回VO数量: " + result.size());
            return result;
            
        } catch (Exception e) {
            System.err.println("获取用户预约列表失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取预约列表失败: " + e.getMessage(), e);
        }
    }

    private Map<Long, Cats> buildCatMap(List<Long> catIds) {
        if (catIds == null || catIds.isEmpty()) {
            return Map.of();
        }
        
        try {
            List<Cats> cats = catsMapper.selectBatchIds(catIds);
            System.out.println("查询到猫咪数量: " + (cats != null ? cats.size() : 0));
            
            if (cats != null && !cats.isEmpty()) {
                return cats.stream()
                        .filter(cat -> cat != null && cat.getId() != null)
                        .collect(Collectors.toMap(Cats::getId, cat -> cat));
            } else {
                return Map.of();
            }
        } catch (Exception e) {
            System.err.println("查询猫咪信息失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of();
        }
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

    @Override
    public List<Long> getBookedTableIds(LocalDate reservationDate, String timeSlot) {
        QueryWrapper<Reservations> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("table_id")
                .eq("reservation_date", reservationDate)
                .eq("time_slot", timeSlot)
                .ne("status", "CANCELLED");

        List<Reservations> reservations = this.list(queryWrapper);
        return reservations.stream()
                .map(Reservations::getTableId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTableReserved(Long tableId, LocalDate reservationDate, String timeSlot) {
        QueryWrapper<Reservations> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("table_id", tableId)
                .eq("reservation_date", reservationDate)
                .eq("time_slot", timeSlot)
                .ne("status", "CANCELLED");

        return this.count(queryWrapper) > 0;
    }
}