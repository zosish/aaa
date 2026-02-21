package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.CatHealthRecords;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 猫咪健康记录表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-11-24
 */
public interface ICatHealthRecordsService extends IService<CatHealthRecords> {
    Page<CatHealthRecords> getCatHealthRecordsList(Map<String, Object> requestParams);
}
