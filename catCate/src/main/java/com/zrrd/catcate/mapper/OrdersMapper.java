package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单主表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

}
