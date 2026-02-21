package com.zrrd.catcatecutomer.controller;

import com.zrrd.catcatecutomer.entity.Users;
import com.zrrd.catcatecutomer.service.IUsersService;
import com.zrrd.catcatecutomer.utils.RedisUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/users")

public class UsersController {
    @Resource
    private IUsersService usersService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 注册
     *
     * @param users
     * @return
     */
    @PostMapping("/register")
    public Integer register(@RequestBody Users users) {
        return usersService.register(users);
    }

    /**
     * 登录
     *
     * @param users
     * @return
     */
    @PostMapping("/login")
    public Users login(@RequestBody Users users) {
        return usersService.login(users);
    }

    /**
     * 找回密码
     *
     * @param users
     * @return
     */
    @PostMapping("/forgot")
    public Boolean forgot(@RequestBody Users users) {
        return null;
    }

    /**
     * 获取当前用户完整信息
     * @param request HTTP请求对象，用于获取token
     * @return 用户完整信息
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentUserInfo(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从请求头中获取token
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // 去掉"Bearer "前缀
            }

            if (token == null || token.isEmpty()) {
                result.put("code", 401);
                result.put("message", "未提供认证信息");
                return result;
            }

            // 通过token获取用户ID
            Long userId = redisUtil.getUserIdByToken(token);
            if (userId == null) {
                result.put("code", 401);
                result.put("message", "无效的认证信息或已过期");
                return result;
            }

            // 查询用户完整信息
            Users user = usersService.getById(userId);
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }

            // 移除敏感信息
            user.setPassword(null);

            result.put("code", 200);
            result.put("message", "获取用户信息成功");
            result.put("data", user);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取用户信息异常：" + e.getMessage());
        }
        return result;
    }

    /**
     * 修改用户密码
     */
    @PutMapping("/password")
    public Map<String, Object> changePassword(@RequestBody Map<String, Object> passwordData) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = Long.valueOf(passwordData.get("userId").toString());
            String oldPassword = passwordData.get("oldPassword").toString();
            String newPassword = passwordData.get("newPassword").toString();

            boolean success = usersService.changePassword(userId, oldPassword, newPassword);

            if (success) {
                result.put("code", 200);
                result.put("message", "密码修改成功");
            } else {
                result.put("code", 400);
                result.put("message", "原密码错误或修改失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "修改密码异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 更新用户个人信息
     */
    @PutMapping("/profile")
    public Map<String, Object> updateUserProfile(@RequestBody Users user) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = usersService.updateById(user);

            if (success) {
                result.put("code", 200);
                result.put("message", "个人信息更新成功");
                result.put("data", user);
            } else {
                result.put("code", 500);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新个人信息异常：" + e.getMessage());
        }

        return result;
    }
}