package com.zrrd.catcate.controller;

import com.zrrd.catcate.entity.VO.UserProductVO;
import com.zrrd.catcate.service.IOrderItemsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 订单商品明细表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/orderItems")
public class OrderItemsController {
    @Resource
    private IOrderItemsService orderItemsService;

    /**
     * 获取所有用户购买的商品信息（管理员专用）
     * @return
     */
    @GetMapping("/sales")
    public List<UserProductVO> getAllUserProducts() {
        return orderItemsService.getAllUserProductInfo();
    }
}
