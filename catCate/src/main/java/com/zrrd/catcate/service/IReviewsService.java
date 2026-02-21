package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Reviews;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 评价表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
public interface IReviewsService extends IService<Reviews> {
    Page<Reviews> getReviewsList(Map<String, Object> requestParams);

    /**
     * 获取待审核的评价数量
     */
    int getPendingReviewCount();
}
