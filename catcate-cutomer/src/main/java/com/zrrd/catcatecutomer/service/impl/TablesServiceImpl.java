package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.Reservations;
import com.zrrd.catcatecutomer.entity.Tables;
import com.zrrd.catcatecutomer.mapper.ReservationsMapper;
import com.zrrd.catcatecutomer.mapper.TablesMapper;
import com.zrrd.catcatecutomer.service.ITablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 桌号信息表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2026-05-13
 */
@Service
public class TablesServiceImpl extends ServiceImpl<TablesMapper, Tables> implements ITablesService {

    @Autowired
    private ReservationsMapper reservationsMapper;

    @Override
    public List<Tables> getAvailableTables() {
        QueryWrapper<Tables> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "AVAILABLE");
        return this.list(queryWrapper);
    }

    /**
     * 解析时间段字符串为开始和结束时间
     */
    private LocalTime[] parseTimeSlot(String timeSlot) {
        if (timeSlot == null || !timeSlot.contains("-")) {
            return null;
        }
        try {
            String[] parts = timeSlot.split("-");
            if (parts.length != 2) return null;
            
            LocalTime startTime = LocalTime.parse(parts[0]);
            LocalTime endTime = LocalTime.parse(parts[1]);
            return new LocalTime[]{startTime, endTime};
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查两个时间段是否有重叠
     */
    private boolean isTimeOverlapping(LocalTime start1, LocalTime end1, 
                                      LocalTime start2, LocalTime end2) {
        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    @Override
    public List<Tables> getAvailableTablesByTime(LocalDate reservationDate, String timeSlot) {
        // 先获取所有可用桌号
        QueryWrapper<Tables> tableQuery = new QueryWrapper<>();
        tableQuery.eq("status", "AVAILABLE");
        List<Tables> allAvailableTables = this.list(tableQuery);
        
        // 获取所有预约记录
        QueryWrapper<Reservations> reservationQuery = new QueryWrapper<>();
        reservationQuery.eq("reservation_date", reservationDate)
                .ne("status", "CANCELLED");
        List<Reservations> allReservations = reservationsMapper.selectList(reservationQuery);

        // 解析请求的时间段
        LocalTime[] requestTime = parseTimeSlot(timeSlot);
        if (requestTime == null) {
            return allAvailableTables;
        }

        // 筛选出有冲突的桌号ID
        Set<Long> conflictingTableIds = allReservations.stream()
                .filter(reservation -> {
                    if (reservation.getTableId() == null) return false;
                    LocalTime[] reservationTime = parseTimeSlot(reservation.getTimeSlot());
                    if (reservationTime == null) return false;
                    return isTimeOverlapping(requestTime[0], requestTime[1], 
                                           reservationTime[0], reservationTime[1]);
                })
                .map(Reservations::getTableId)
                .collect(Collectors.toSet());

        // 返回没有冲突的桌号
        return allAvailableTables.stream()
                .filter(table -> !conflictingTableIds.contains(table.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTableAvailable(Long tableId, LocalDate reservationDate, String timeSlot) {
        QueryWrapper<Reservations> reservationQuery = new QueryWrapper<>();
        reservationQuery.eq("table_id", tableId)
                .eq("reservation_date", reservationDate)
                .ne("status", "CANCELLED");
        List<Reservations> existingReservations = reservationsMapper.selectList(reservationQuery);

        // 解析请求的时间段
        LocalTime[] requestTime = parseTimeSlot(timeSlot);
        if (requestTime == null) {
            return true;
        }

        // 检查是否有时间重叠
        for (Reservations reservation : existingReservations) {
            LocalTime[] reservationTime = parseTimeSlot(reservation.getTimeSlot());
            if (reservationTime == null) continue;
            
            if (isTimeOverlapping(requestTime[0], requestTime[1], 
                                  reservationTime[0], reservationTime[1])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean reserveTable(Long tableId) {
        UpdateWrapper<Tables> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", tableId)
                .eq("status", "AVAILABLE")
                .set("status", "RESERVED")
                .set("update_time", LocalDateTime.now());

        return this.update(updateWrapper);
    }

    @Override
    public boolean releaseTable(Long tableId) {
        UpdateWrapper<Tables> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", tableId)
                .eq("status", "RESERVED")
                .set("status", "AVAILABLE")
                .set("update_time", LocalDateTime.now());

        return this.update(updateWrapper);
    }
}