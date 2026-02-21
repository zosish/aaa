package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.Cats;
import com.zrrd.catcatecutomer.mapper.CatsMapper;
import com.zrrd.catcatecutomer.service.ICatsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 猫咪信息表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Service
public class CatsServiceImpl extends ServiceImpl<CatsMapper, Cats> implements ICatsService {

    @Override
    public Page<Cats> getCatsList(Map<String, Object> params) {
        // 获取分页参数
        int current = params.get("current") != null ?
                Integer.parseInt(params.get("current").toString()) : 1;
        int size = params.get("size") != null ?
                Integer.parseInt(params.get("size").toString()) : 10;

        Page<Cats> page = new Page<>(current, size);

        // 构建查询条件
        QueryWrapper<Cats> queryWrapper = new QueryWrapper<>();

        // 名称模糊查询
        if (params.containsKey("name") && StringUtils.hasText(params.get("name").toString())) {
            queryWrapper.like("name", params.get("name"));
        }

        // 品种筛选
        if (params.containsKey("breed") && StringUtils.hasText(params.get("breed").toString())) {
            queryWrapper.eq("breed", params.get("breed"));
        }

        // 性别筛选
        if (params.containsKey("gender") && StringUtils.hasText(params.get("gender").toString())) {
            queryWrapper.eq("gender", params.get("gender"));
        }

        // 领养状态筛选
        if (params.containsKey("adoptionStatus") && StringUtils.hasText(params.get("adoptionStatus").toString())) {
            queryWrapper.eq("adoption_status", params.get("adoptionStatus"));
        }

        // 健康状态筛选
        if (params.containsKey("healthStatus") && StringUtils.hasText(params.get("healthStatus").toString())) {
            queryWrapper.eq("health_status", params.get("healthStatus"));
        }

        // 是否活跃筛选
        if (params.containsKey("isActive")) {
            queryWrapper.eq("is_active", params.get("isActive"));
        }

        // 按创建时间降序排列
        queryWrapper.orderByDesc("create_time");

        return this.page(page, queryWrapper);
    }

    @Override
    public List<Cats> getAvailableCats() {
        QueryWrapper<Cats> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", (byte) 1)
                .eq("adoption_status", "AVAILABLE")
                .orderByDesc("create_time");

        return this.list(queryWrapper);
    }

    @Override
    public List<Cats> getPopularCats(int limit) {
        // 调用Mapper中的自定义方法获取热门猫咪
        return this.baseMapper.getPopularCats(limit);
    }
}