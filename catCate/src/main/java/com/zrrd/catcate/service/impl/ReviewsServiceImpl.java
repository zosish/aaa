package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Activities;
import com.zrrd.catcate.entity.Reviews;
import com.zrrd.catcate.mapper.ReviewsMapper;
import com.zrrd.catcate.service.IReviewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评价表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Service
public class ReviewsServiceImpl extends ServiceImpl<ReviewsMapper, Reviews> implements IReviewsService {
    @Resource
    private ReviewsMapper reviewsMapper;
    @Override
    public Page<Reviews> getReviewsList(Map<String, Object> params) {
        // 获取分页参数
        int pageNum = (int) params.getOrDefault("pageNum", 1);
        int pageSize = (int) params.getOrDefault("pageSize", 10);
        int offset = (pageNum - 1) * pageSize;

        // 设置分页参数
        params.put("offset", offset);
        params.put("pageSize", pageSize);

        // 查询数据
        List<Reviews> records = reviewsMapper.selectReviewsNoNull(params);

        // 查询总数
        int total = reviewsMapper.countReviewsNoNull(params);

        // 创建分页对象并设置数据
        Page<Reviews> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(total);

        return page;
    }

    /**
     * 获取待审核的评价数量
     */
    public int getPendingReviewCount() {
        QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("COUNT(*) as count")
                .eq("review_status", "PENDING");

        Map<String, Object> result = this.getBaseMapper().selectMaps(queryWrapper).get(0);
        return result.get("count") != null ? Integer.parseInt(result.get("count").toString()) : 0;
    }
}
