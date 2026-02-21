package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Reviews;
import com.zrrd.catcate.service.IReviewsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评价表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/reviews")
public class ReviewsController {
    @Resource
    private IReviewsService reviewsService;
    /**
     * 获取评价列表（带搜索和分页）
     *
     * @param requestParams 搜索参数
     * @return 分页结果
     */
    @PostMapping("/selectList")
    public Map<String, Object> selectReviewsList(@RequestBody Map<String, Object> requestParams){
        System.out.println("============================" + requestParams);
        // 执行分页查询
        Page<Reviews> page = reviewsService.getReviewsList(requestParams);
        // 构造返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", page.getTotal());
        response.put("list", page.getRecords());

        return response;
    }

    /**
     * 通过或拒绝评论
     */
    @PostMapping("/updateStatus")
    public boolean updateStatus( @RequestBody Reviews reviews){
        return reviewsService.updateById(reviews);
    }

    /**
     * 管理员回复(现在好像只能保存一次，后需要修改)
     */
    @PostMapping("/reply")
    public boolean reply(@RequestBody Reviews reviews){
        return reviewsService.updateById(reviews);
    }
    /**
     * 批量修改
     */
    @PostMapping("/batchUpdateStatus")
    public boolean batchUpdateStatus(@RequestBody List<Reviews> reviews){
        return reviewsService.updateBatchById(reviews);
    }

    /**
     * 删除评论
     */
    @PostMapping("/deleteById")
    public boolean deleteById(@RequestBody Reviews reviews){
        return reviewsService.removeById(reviews);
    }

    /**
     * 批量删除评论
     */
    @PostMapping("/deleteByIds")
    public boolean deleteByIds(@RequestBody List<Long> ids){
        return reviewsService.removeByIds(ids);
    }
}
