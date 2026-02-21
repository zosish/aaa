package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Activities;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 活动信息表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
public interface IActivitiesService extends IService<Activities> {
    Page<Activities> getActivityList(Map<String, Object> requestParams);
}
