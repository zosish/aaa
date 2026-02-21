package com.zrrd.catcatecutomer.service;

import com.zrrd.catcatecutomer.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface IUsersService extends IService<Users> {
    Integer register(Users users);
    Users login(Users users);  // 修改返回类型为Users对象
    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}