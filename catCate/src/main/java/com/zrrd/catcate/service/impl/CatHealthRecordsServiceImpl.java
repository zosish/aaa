package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Activities;
import com.zrrd.catcate.entity.CatHealthRecords;
import com.zrrd.catcate.mapper.CatHealthRecordsMapper;
import com.zrrd.catcate.service.ICatHealthRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 猫咪健康记录表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-11-24
 */
@Service
public class CatHealthRecordsServiceImpl extends ServiceImpl<CatHealthRecordsMapper, CatHealthRecords> implements ICatHealthRecordsService {
    @Resource
    private CatHealthRecordsMapper catHealthRecoradsMapper;

    @Override
    public Page<CatHealthRecords> getCatHealthRecordsList(Map<String, Object> requestParams) {
        // 获取分页参数
        int pageNum = (int) requestParams.getOrDefault("pageNum", 1);
        int pageSize = (int) requestParams.getOrDefault("pageSize", 10);
        int offset = (pageNum - 1) * pageSize;

        // 设置分页参数
        requestParams.put("offset", offset);
        requestParams.put("pageSize", pageSize);

        // 参数映射：将前端的symptomsKeyword映射为Mapper需要的symptoms参数
        if (requestParams.containsKey("symptomsKeyword") && requestParams.get("symptomsKeyword") != null) {
            requestParams.put("symptoms", requestParams.get("symptomsKeyword"));
        }

        // 查询数据
        List<CatHealthRecords> records = catHealthRecoradsMapper.selectCatHealthRecordsNoNull(requestParams);

        // 查询总数
        int total = catHealthRecoradsMapper.countCatHealthRecordsNoNull(requestParams);

        // 创建分页对象并设置数据
        Page<CatHealthRecords> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(total);

        return page;
    }
}
