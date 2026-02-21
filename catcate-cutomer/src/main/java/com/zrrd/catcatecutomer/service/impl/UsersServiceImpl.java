package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zrrd.catcatecutomer.entity.Users;
import com.zrrd.catcatecutomer.mapper.UsersMapper;
import com.zrrd.catcatecutomer.service.IUsersService;
import com.zrrd.catcatecutomer.utils.RedisUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    @Resource
    private UsersMapper usersMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public Integer register(Users users) {
        // 插入用户时把role字段自动置为客户
        return usersMapper.insert(users);
    }

    @Override
    public Users login(Users users) {
        QueryWrapper<Users> qw = new QueryWrapper<>();
        qw.eq("status", "active").eq("role", "CUSTOMER").eq("username", users.getUsername()).eq("password", users.getPassword());

        Users loginUser = usersMapper.selectOne(qw);
        if (loginUser != null) {
            // 生成唯一token
            String token = UUID.randomUUID().toString().replace("-", "");

            // 更新最后登录时间
            UpdateWrapper<Users> uw = new UpdateWrapper<>();
            uw.eq("id", loginUser.getId());
            uw.set("last_login_time", LocalDateTime.now());
            usersMapper.update(null, uw);

            // 将用户ID存储到Redis中，设置过期时间为2小时
            redisUtil.setUserCache(token, loginUser.getId(), 7200);

            // 设置token到返回对象中（不会存储到数据库）
            loginUser.setToken(token);

            return loginUser;
        } else {
            return null;
        }
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            // 先验证原密码是否正确
            Users user = this.getById(userId);
            if (user == null) {
                return false;
            }

            if (!user.getPassword().equals(oldPassword)) {
                return false; // 原密码不正确
            }

            // 更新密码
            Users updateUser = new Users();
            updateUser.setId(userId);
            updateUser.setPassword(newPassword);
            updateUser.setUpdateTime(LocalDateTime.now());

            return this.updateById(updateUser);
        } catch (Exception e) {
            return false;
        }
    }
}