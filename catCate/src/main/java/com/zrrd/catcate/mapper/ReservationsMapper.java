package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.Reservations;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 预约记录表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */

@Mapper
public interface ReservationsMapper extends BaseMapper<Reservations> {
    List<Reservations> listAppointment(Map<String, Object> params);
//    条件搜索
    List<Reservations> selectNoNull(Reservations reservations);
    int countNoNull(Map<String, Object>  params);
}
