package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Reservations;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 预约记录表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
public interface IReservationsService extends IService<Reservations> {
    Page<Reservations> listAppointment(Map<String, Object> params);
    boolean updateReservationStatus(Long id, String status);
//    List<Reservations> conditionList(Map<String, Object> params);

}
