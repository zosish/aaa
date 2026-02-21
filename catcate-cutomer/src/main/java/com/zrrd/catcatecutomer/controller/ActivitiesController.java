package com.zrrd.catcatecutomer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcatecutomer.entity.Activities;
import com.zrrd.catcatecutomer.service.IActivitiesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动信息表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/activities")
public class ActivitiesController {

    @Resource
    private IActivitiesService activitiesService;

    /**
     * 分页查询活动列表（用于套餐选择等场景）
     */
    @PostMapping("/selectList")
    public Map<String, Object> selectList(@RequestBody Map<String, Object> requestParams) {
        try {
            Page<Activities> page = activitiesService.getActivityList(requestParams);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", page.getRecords());
            response.put("records", page.getRecords());
            response.put("list", page.getRecords());
            response.put("total", page.getTotal());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 客户端获取活动列表（优先显示未过期且推荐的活动）
     */
    @GetMapping("/client/list")
    public Map<String, Object> getClientActivities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String activityType) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("keyword", keyword);
            params.put("activityType", activityType);

            List<Activities> activities = activitiesService.getClientActivities(params);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", activities);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 获取最新的未过期活动（用于首页展示）
     */
    @GetMapping("/recent")
    public Map<String, Object> getRecentActivities(@RequestParam(defaultValue = "3") Integer limit) {
        try {
            List<Activities> recentActivities = activitiesService.getRecentActivities(limit);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", recentActivities);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/client/detail/{id}")
    public Map<String, Object> getActivityDetail(@PathVariable Long id) {
        try {
            Activities activity = activitiesService.getById(id);
            Map<String, Object> response = new HashMap<>();
            if (activity != null && "ACTIVE".equals(activity.getStatus())) {
                response.put("code", 200);
                response.put("message", "查询成功");
                response.put("data", activity);

                // 增加浏览次数
                activity.setViewCount(activity.getViewCount() != null ? activity.getViewCount() + 1 : 1);
                activitiesService.updateById(activity);
            } else {
                response.put("code", 404);
                response.put("message", "活动不存在或已结束");
            }
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }
}