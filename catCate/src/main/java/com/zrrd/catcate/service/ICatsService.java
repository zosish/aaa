package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Cats;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 猫咪信息表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
public interface ICatsService extends IService<Cats> {
    /**
     * 条件查询
     * @param cat
     */
    List<Cats> conditionList(Cats cat);
    List<Cats> listActive();
    Page<Cats> getCatsList(Map<String, Object> requestParams);
}
