package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.Activities;
import com.zrrd.catcatecutomer.mapper.ActivitiesMapper;
import com.zrrd.catcatecutomer.service.IActivitiesService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动信息表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Service
public class ActivitiesServiceImpl extends ServiceImpl<ActivitiesMapper, Activities> implements IActivitiesService {

    @Override
    public Page<Activities> getActivityList(Map<String, Object> requestParams) {
        // 获取分页参数
        int current = requestParams.get("current") != null ?
                Integer.parseInt(requestParams.get("current").toString()) : 1;
        int size = requestParams.get("size") != null ?
                Integer.parseInt(requestParams.get("size").toString()) : 10;

        Page<Activities> page = new Page<>(current, size);

        // 构建查询条件
        QueryWrapper<Activities> queryWrapper = new QueryWrapper<>();

        // 活动类型筛选
        if (requestParams.containsKey("activityType") &&
                StringUtils.hasText(requestParams.get("activityType").toString())) {
            queryWrapper.eq("activity_type", requestParams.get("activityType"));
        }

        // 状态筛选
        if (requestParams.containsKey("status") &&
                StringUtils.hasText(requestParams.get("status").toString())) {
            queryWrapper.eq("status", requestParams.get("status"));
        }

        // 标题模糊查询
        if (requestParams.containsKey("title") &&
                StringUtils.hasText(requestParams.get("title").toString())) {
            queryWrapper.like("title", requestParams.get("title"));
        }

        // 是否推荐筛选
        if (requestParams.containsKey("isFeatured")) {
            queryWrapper.eq("is_featured", requestParams.get("isFeatured"));
        }

        // 按创建时间降序排列
        queryWrapper.orderByDesc("create_time");

        return this.page(page, queryWrapper);
    }

    @Override
    public List<Activities> getPackageActivities() {
        QueryWrapper<Activities> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_type", "SETMEAL")
                .eq("status", "ACTIVE")
                .orderByDesc("create_time");

        return this.list(queryWrapper);
    }

    @Override
    public List<Activities> getClientActivities(Map<String, Object> params) {
        QueryWrapper<Activities> queryWrapper = new QueryWrapper<>();

        // 只查询进行中的活动
        queryWrapper.eq("status", "ACTIVE");
        // 只查询未过期的活动
        queryWrapper.gt("end_time", LocalDateTime.now());

        // 活动类型筛选
        if (params.containsKey("activityType") && params.get("activityType") != null) {
            queryWrapper.eq("activity_type", params.get("activityType"));
        }

        // 关键词搜索
        if (params.containsKey("keyword") && params.get("keyword") != null) {
            String keyword = params.get("keyword").toString();
            queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("content", keyword));
        }

        // 优先显示推荐的活动，然后按结束时间升序排列
        queryWrapper.orderByDesc("is_featured")
                .orderByAsc("end_time")
                .orderByDesc("create_time");

        return this.list(queryWrapper);
    }

    @Override
    public List<Activities> getRecentActivities(Integer limit) {
        QueryWrapper<Activities> queryWrapper = new QueryWrapper<>();

        // 只查询进行中的活动
        queryWrapper.eq("status", "ACTIVE");
        // 只查询未过期的活动
        queryWrapper.gt("end_time", LocalDateTime.now());
        // 限制数量
        queryWrapper.last("LIMIT " + limit);

        // 按创建时间降序排列（最新的在前面）
        queryWrapper.orderByDesc("create_time");

        return this.list(queryWrapper);
    }
}