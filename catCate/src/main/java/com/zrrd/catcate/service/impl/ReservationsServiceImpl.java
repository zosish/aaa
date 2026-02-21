package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Activities;
import com.zrrd.catcate.entity.Cats;
import com.zrrd.catcate.entity.Reservations;
import com.zrrd.catcate.entity.Users;
import com.zrrd.catcate.mapper.CatsMapper;
import com.zrrd.catcate.mapper.ReservationsMapper;
import com.zrrd.catcate.mapper.UsersMapper;
import com.zrrd.catcate.service.IReservationsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.object.UpdatableSqlQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 预约记录表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Service
public class ReservationsServiceImpl extends ServiceImpl<ReservationsMapper, Reservations> implements IReservationsService {
    @Resource
    private ReservationsMapper reservationsMapper;
    @Resource
    private UsersMapper usersMapper;
    @Autowired
    private CatsMapper catsMapper;

    @Override
    public Page<Reservations> listAppointment(Map<String, Object> params) {
        int pageNum = (int) params.getOrDefault("pageNum", 1);
        int pageSize = (int) params.getOrDefault("pageSize", 10);
        int offset = (pageNum - 1) * pageSize;
        // 设置分页参数
        params.put("offset", offset);
        params.put("pageSize", pageSize);

        // 查询数据
        List<Reservations> records = reservationsMapper.listAppointment(params);

        // 查询总数
        int total = reservationsMapper.countNoNull(params);

        // 创建分页对象并设置数据
        Page<Reservations> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(total);

        return page;
    }

    @Override
    public boolean updateReservationStatus(Long id, String status) {
        Reservations reservations = new Reservations();
        reservations.setId(id);
        reservations.setStatus(status);
        UpdateWrapper<Reservations> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("status", status);
        return this.update(reservations, updateWrapper);
    }

//    @Override
//    public List<Reservations> conditionList(Reservations reservations) {
//        System.out.println( reservations);
//        //模糊查询
//        QueryWrapper<Reservations> queryWrapper = new QueryWrapper<>();
//        queryWrapper.like(reservations.getCatId() != null, "cat_id", reservations.getCatId())
//                .like(reservations.getReservationDate() != null, "reservation_date", reservations.getReservationDate())
//                .like(reservations.getStatus() != null, "status", reservations.getStatus())
//                .like(reservations.getTimeSlot() != null, "time_slot", reservations.getTimeSlot());
//        return this.list(queryWrapper);
//    }


}
