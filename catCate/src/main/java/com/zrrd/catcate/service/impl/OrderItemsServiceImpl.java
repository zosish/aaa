package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zrrd.catcate.entity.OrderItems;
import com.zrrd.catcate.entity.VO.UserProductVO;
import com.zrrd.catcate.mapper.OrderItemsMapper;
import com.zrrd.catcate.service.IOrderItemsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单商品明细表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Service
public class OrderItemsServiceImpl extends ServiceImpl<OrderItemsMapper, OrderItems> implements IOrderItemsService {

    @Resource
    private OrderItemsMapper orderItemsMapper;

    /**
     * 获取所有用户购买的商品信息
     * @return
     */
    @Override
    public List<UserProductVO> getAllUserProductInfo() {
        return orderItemsMapper.selectAllUserProductInfo();
    }

    /**
     * 根据订单ID获取订单项列表
     * @param orderId 订单ID
     * @return 订单项列表
     */
    @Override
    public List<OrderItems> getByOrderId(Long orderId) {
        QueryWrapper<OrderItems> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        return this.list(queryWrapper);
    }
}
