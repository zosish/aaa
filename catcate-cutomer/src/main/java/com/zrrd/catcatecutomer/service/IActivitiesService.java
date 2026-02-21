package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zrrd.catcatecutomer.entity.Activities;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动信息表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface IActivitiesService extends IService<Activities> {

    /**
     * 分页查询活动列表
     * @param requestParams 查询参数
     * @return 分页结果
     */
    Page<Activities> getActivityList(Map<String, Object> requestParams);

    /**
     * 查询套餐类型活动列表
     * @return 套餐活动列表
     */
    List<Activities> getPackageActivities();
    /**
     * 获取客户端活动列表（优先显示未过期且推荐的活动）
     * @param params 查询参数
     * @return 活动列表
     */
    List<Activities> getClientActivities(Map<String, Object> params);

    /**
     * 获取最新的未过期活动（用于首页展示）
     * @param limit 限制数量
     * @return 最近活动列表
     */
    List<Activities> getRecentActivities(Integer limit);
}