package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Activities;
import com.zrrd.catcate.entity.Products;
import com.zrrd.catcate.mapper.ActivitiesMapper;
import com.zrrd.catcate.service.IActivitiesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动信息表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Service
public class ActivitiesServiceImpl extends ServiceImpl<ActivitiesMapper, Activities> implements IActivitiesService {
    @Resource
    private ActivitiesMapper activitiesMapper;

    @Override
    public Page<Activities> getActivityList(Map<String, Object> requestParams) {
        // 获取分页参数
        int pageNum = (int) requestParams.getOrDefault("pageNum", 1);
        int pageSize = (int) requestParams.getOrDefault("pageSize", 10);
        int offset = (pageNum - 1) * pageSize;

        // 设置分页参数
        requestParams.put("offset", offset);
        requestParams.put("pageSize", pageSize);

        // 查询数据
        List<Activities> records = activitiesMapper.selectActivitiesNoNull(requestParams);

        // 查询总数
        int total = activitiesMapper.countActivitiesNoNull(requestParams);

        // 创建分页对象并设置数据
        Page<Activities> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(total);

        return page;
    }
}
