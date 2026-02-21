package com.zrrd.catcate.service;

import com.zrrd.catcate.entity.OrderItems;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zrrd.catcate.entity.VO.UserProductVO;

import java.util.List;

/**
 * <p>
 * 订单商品明细表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
public interface IOrderItemsService extends IService<OrderItems> {
    /**
     * 获取所有用户购买的商品信息
     * @return
     */
    List<UserProductVO> getAllUserProductInfo();

    /**
     * 根据订单ID获取订单项列表
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItems> getByOrderId(Long orderId);

}
