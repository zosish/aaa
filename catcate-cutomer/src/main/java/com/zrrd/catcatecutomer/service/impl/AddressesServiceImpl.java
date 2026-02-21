package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.Addresses;
import com.zrrd.catcatecutomer.mapper.AddressesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zrrd.catcatecutomer.service.IAddressesService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;


import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddressesServiceImpl extends ServiceImpl<AddressesMapper, Addresses> implements IAddressesService {

    @Override
    public List<Addresses> getUserAddresses(Long userId) {
        QueryWrapper<Addresses> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("is_default")
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public Addresses getUserDefaultAddress(Long userId) {
        QueryWrapper<Addresses> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("is_default", 1)
                .orderByDesc("update_time")
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean setDefaultAddress(Long addressId, Long userId) {
        try {
            // 检查地址是否存在且属于该用户
            Addresses targetAddress = this.getById(addressId);
            if (targetAddress == null || !targetAddress.getUserId().equals(userId)) {
                return false;
            }

            // 检查是否已经是默认地址
            if (targetAddress.getIsDefault() != null && targetAddress.getIsDefault() == 1) {
                return true; // 已经是默认地址，无需操作
            }

            // 开启事务，确保原子性操作
            // 先将该用户的所有地址设为非默认
            QueryWrapper<Addresses> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("user_id", userId);
            Addresses nonDefaultAddress = new Addresses();
            nonDefaultAddress.setIsDefault(0);
            this.update(nonDefaultAddress, updateWrapper);

            // 再将指定地址设为默认
            Addresses defaultAddress = new Addresses();
            defaultAddress.setId(addressId);
            defaultAddress.setIsDefault(1);
            defaultAddress.setUpdateTime(java.time.LocalDateTime.now());
            boolean result = this.updateById(defaultAddress);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 验证并修复用户的默认地址（确保只有一个默认地址）
     */
    @Transactional
    @Override
    public void validateAndFixDefaultAddress(Long userId) {
        try {
            QueryWrapper<Addresses> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("is_default", 1);
            List<Addresses> defaultAddresses = this.list(queryWrapper);

            if (defaultAddresses.size() > 1) {
                // 如果有多个默认地址，只保留创建时间最早的作为默认地址
                defaultAddresses.sort(Comparator.comparing(Addresses::getCreateTime));

                // 将除了第一个之外的所有地址设为非默认
                for (int i = 1; i < defaultAddresses.size(); i++) {
                    Addresses address = defaultAddresses.get(i);
                    address.setIsDefault(0);
                    address.setUpdateTime(java.time.LocalDateTime.now());
                    this.updateById(address);
                }

                System.out.println("已修复用户 " + userId + " 的重复默认地址问题");
            } else if (defaultAddresses.isEmpty()) {
                // 如果没有默认地址，将最早创建的地址设为默认
                QueryWrapper<Addresses> allAddressesWrapper = new QueryWrapper<>();
                allAddressesWrapper.eq("user_id", userId)
                        .orderByAsc("create_time")
                        .last("LIMIT 1");
                Addresses firstAddress = this.getOne(allAddressesWrapper);

                if (firstAddress != null) {
                    firstAddress.setIsDefault(1);
                    firstAddress.setUpdateTime(java.time.LocalDateTime.now());
                    this.updateById(firstAddress);
                    System.out.println("已为用户 " + userId + " 设置默认地址");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public boolean setDefaultAddressForNewAddress(Addresses newAddress) {
        try {
            // 先保存新地址
            newAddress.setCreateTime(LocalDateTime.now());
            newAddress.setUpdateTime(LocalDateTime.now());
            newAddress.setIsDefault(0); // 先设为非默认

            boolean saved = this.save(newAddress);
            if (!saved) {
                return false;
            }

            // 然后将其设为默认地址
            return setDefaultAddress(newAddress.getId(), newAddress.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}