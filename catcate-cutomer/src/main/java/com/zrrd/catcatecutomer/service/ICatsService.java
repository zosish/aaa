package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zrrd.catcatecutomer.entity.Cats;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 猫咪信息表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
public interface ICatsService extends IService<Cats> {

    /**
     * 分页查询猫咪列表
     * @param params 查询参数
     * @return 分页结果
     */
    Page<Cats> getCatsList(Map<String, Object> params);

    /**
     * 获取可领养的活跃猫咪列表
     * @return 可领养的猫咪列表
     */
    List<Cats> getAvailableCats();

    /**
     * 获取预约次数最多的热门猫咪
     * @param limit 返回数量，默认4个
     * @return 热门猫咪列表
     */
    List<Cats> getPopularCats(int limit);
}