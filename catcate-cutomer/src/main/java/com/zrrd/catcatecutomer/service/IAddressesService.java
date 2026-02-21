package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zrrd.catcatecutomer.entity.Addresses;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IAddressesService extends IService<Addresses> {

    /**
     * 获取用户收货地址列表
     */
    List<Addresses> getUserAddresses(Long userId);

    /**
     * 获取用户默认地址
     */
    Addresses getUserDefaultAddress(Long userId);

    /**
     * 设置默认地址
     */
    boolean setDefaultAddress(Long addressId, Long userId);

    /**
     * 为新地址设置默认地址（事务安全）
     */
    @Transactional
    boolean setDefaultAddressForNewAddress(Addresses newAddress);

    /**
     * 验证并修复用户的默认地址
     */
    @Transactional
    void validateAndFixDefaultAddress(Long userId);
}