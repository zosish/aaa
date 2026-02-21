package com.zrrd.catcatecutomer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zrrd.catcatecutomer.entity.Addresses;
import com.zrrd.catcatecutomer.service.IAddressesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 收货地址表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/addresses")
public class AddressesController {

    @Resource
    private IAddressesService addressesService;

    /**
     * 获取用户默认地址
     */
    @GetMapping("/user/{userId}/default")
    public Map<String, Object> getUserDefaultAddress(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Addresses defaultAddress = addressesService.getUserDefaultAddress(userId);

            if (defaultAddress != null) {
                result.put("code", 200);
                result.put("message", "获取默认地址成功");
                result.put("data", defaultAddress);
            } else {
                result.put("code", 404);
                result.put("message", "未找到默认地址");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取默认地址异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户收货地址列表
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserAddresses(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Addresses> addresses = addressesService.getUserAddresses(userId);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", addresses);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取用户地址异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 添加收货地址
     */
    @PostMapping
    public Map<String, Object> addAddress(@RequestBody Addresses address) {
        Map<String, Object> result = new HashMap<>();

        try {
            address.setCreateTime(LocalDateTime.now());
            address.setUpdateTime(LocalDateTime.now());

            // 验证必填字段
            if (address.getUserId() == null) {
                result.put("code", 400);
                result.put("message", "用户ID不能为空");
                return result;
            }

            if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "收货人姓名不能为空");
                return result;
            }

            if (address.getPhone() == null || address.getPhone().trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "手机号码不能为空");
                return result;
            }

            // 验证手机号格式
            if (!address.getPhone().matches("^1[3-9]\\d{9}$")) {
                result.put("code", 400);
                result.put("message", "手机号码格式不正确");
                return result;
            }

            // 检查该用户是否已经有地址
            List<Addresses> userAddresses = addressesService.getUserAddresses(address.getUserId());

            // 如果明确要求设为默认地址
            if (address.getIsDefault() != null && address.getIsDefault() == 1) {
                // 使用事务确保只有一个默认地址
                boolean success = addressesService.setDefaultAddressForNewAddress(address);
                if (success) {
                    result.put("code", 200);
                    result.put("message", "地址添加成功并设为默认");
                    result.put("data", address);
                } else {
                    result.put("code", 500);
                    result.put("message", "地址添加失败");
                }
                return result;
            }

            // 如果没有明确指定，默认为非默认地址
            address.setIsDefault(0);

            boolean success = addressesService.save(address);

            if (success) {
                // 如果这是用户的第一条地址，自动设为默认
                if (userAddresses.isEmpty()) {
                    address.setIsDefault(1);
                    addressesService.updateById(address);
                }

                result.put("code", 200);
                result.put("message", "地址添加成功");
                result.put("data", address);
            } else {
                result.put("code", 500);
                result.put("message", "地址添加失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加地址异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateAddress(@PathVariable Long id, @RequestBody Addresses address) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取原始地址信息
            Addresses originalAddress = addressesService.getById(id);
            if (originalAddress == null) {
                result.put("code", 404);
                result.put("message", "地址不存在");
                return result;
            }

            address.setId(id);
            address.setUpdateTime(LocalDateTime.now());

            // 如果要设置为默认地址，使用专门的设置默认地址方法
            if (address.getIsDefault() != null && address.getIsDefault() == 1) {
                boolean success = addressesService.setDefaultAddress(id, originalAddress.getUserId());
                if (success) {
                    result.put("code", 200);
                    result.put("message", "地址更新成功并设为默认");
                } else {
                    result.put("code", 500);
                    result.put("message", "地址更新失败");
                }
            } else {
                // 普通更新
                boolean success = addressesService.updateById(address);
                if (success) {
                    result.put("code", 200);
                    result.put("message", "地址更新成功");
                } else {
                    result.put("code", 500);
                    result.put("message", "地址更新失败");
                }
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新地址异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 删除收货地址
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteAddress(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = addressesService.removeById(id);

            if (success) {
                result.put("code", 200);
                result.put("message", "地址删除成功");
            } else {
                result.put("code", 500);
                result.put("message", "地址删除失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除地址异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/{id}/default")
    public Map<String, Object> setDefaultAddress(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 通过地址ID获取地址信息，从而获得用户ID
            Addresses address = addressesService.getById(id);
            if (address == null) {
                result.put("code", 404);
                result.put("message", "地址不存在");
                return result;
            }

            boolean success = addressesService.setDefaultAddress(id, address.getUserId());

            if (success) {
                result.put("code", 200);
                result.put("message", "设置默认地址成功");
            } else {
                result.put("code", 500);
                result.put("message", "设置默认地址失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "设置默认地址异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 验证并修复用户的默认地址
     */
    @PostMapping("/validate-default/{userId}")
    public Map<String, Object> validateDefaultAddress(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            addressesService.validateAndFixDefaultAddress(userId);

            result.put("code", 200);
            result.put("message", "默认地址验证完成");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "验证默认地址异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量验证并修复所有用户的默认地址
     */
    @PostMapping("/validate-all-defaults")
    public Map<String, Object> validateAllDefaultAddresses() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取所有有地址的用户ID
            List<Object> userIds = addressesService.listObjs(
                    new QueryWrapper<Addresses>().select("DISTINCT user_id")
            );

            int fixedCount = 0;
            for (Object userIdObj : userIds) {
                Long userId = ((Number) userIdObj).longValue();
                try {
                    addressesService.validateAndFixDefaultAddress(userId);
                    fixedCount++;
                } catch (Exception e) {
                    System.err.println("修复用户 " + userId + " 的默认地址时出错: " + e.getMessage());
                }
            }

            result.put("code", 200);
            result.put("message", "默认地址批量验证完成");
            result.put("data", Map.of("processedUsers", fixedCount, "totalUsers", userIds.size()));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "批量验证默认地址异常：" + e.getMessage());
        }

        return result;
    }
}