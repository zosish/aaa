package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Users;
import com.zrrd.catcate.service.IUsersService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/users")
public class UsersController {
    @Resource
    private IUsersService usersService;

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
    public Boolean login(@RequestBody Users users) {
        System.out.println("users-----------" + users);
        Boolean login = usersService.login(users);
        System.out.println("login-----------" + login);
        return login;
    }

    @GetMapping("stats")
    //返回四个int类型的数据
    public List<Object> stats() {
        List<Object> stats = usersService.stats();
        System.out.println(stats + "-----------stats-----------");
        return stats;
    }

    /**
     * 找回密码（没写）
     *
     * @param users
     * @return
     */
    @PostMapping("/forgot")
    public Boolean forgot(@RequestBody Users users) {
        return null;
    }

    /**
     * 获取所有(符合条件)用户信息
     */
    @PostMapping("/selectList")
    public Map<String, Object> selectList(@RequestBody Map<String, Object> users) {
        Page<Users> page = usersService.getUsersList(users);
        Map<String, Object> response = new HashMap<>();
        response.put("total", page.getTotal());
        response.put("list", page.getRecords());
        return response;
    }

    /**
     * 添加或修改用户信息
     */
    @PostMapping("/addOrUpdate")
    public boolean addOrUpdate(@RequestBody Users users) {
        return usersService.saveOrUpdate(users);
    }

    /**
     * 修改状态
     */
    @PostMapping("/updateStatus")
    public boolean updateStatus(@RequestBody Users users) {
        return usersService.updateById(users);
    }

    /**
     * 删除用户
     */
    @PostMapping("/deleteById")
    public boolean deleteById(@RequestBody Users users) {
        return usersService.removeById(users);
    }

    /**
     * 批量删除用户
     */
    @PostMapping("/deleteByIds")
    public boolean deleteByIds(@RequestBody List<Users> users) {
        return usersService.removeByIds(users);
    }

    /**
     * 批量修改用户状态
     */
    @PostMapping("/updateByIdStatus")
    public boolean updateByIdStatus(@RequestBody List<Users> users) {
        return usersService.updateBatchById(users);
    }

    /**
     * 获取最近动态数据（支持分页）
     */
    @GetMapping("/recentActivities")
    public Map<String, Object> getRecentActivities(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        List<Map<String, Object>> allActivities = usersService.getRecentActivities();

        // 实现分页逻辑
        int total = allActivities.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Map<String, Object>> paginatedActivities =
                fromIndex < total ? allActivities.subList(fromIndex, toIndex) : new ArrayList<>();

        Map<String, Object> response = new HashMap<>();
        response.put("list", paginatedActivities);
        response.put("total", total);
        response.put("pageNum", pageNum);
        response.put("pageSize", pageSize);
        response.put("pages", (int) Math.ceil((double) total / pageSize));

        return response;
    }
}
