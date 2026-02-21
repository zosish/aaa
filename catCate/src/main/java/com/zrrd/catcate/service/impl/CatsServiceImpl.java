package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Activities;
import com.zrrd.catcate.entity.Cats;
import com.zrrd.catcate.mapper.CatsMapper;
import com.zrrd.catcate.service.ICatsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 猫咪信息表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Service
public class CatsServiceImpl extends ServiceImpl<CatsMapper, Cats> implements ICatsService {

    @Resource
    private CatsMapper catsMapper;
    @Autowired
    private Cats cats;

    @Override
    public List<Cats> conditionList(Cats cat) {
        QueryWrapper<Cats> qw = new QueryWrapper<>();
        qw.like(cat.getName() != null, "name", cat.getName())
                .like(cat.getBreed() != null, "breed", cat.getBreed())
                .like(cat.getHealthStatus() != null, "health_status", cat.getHealthStatus())
                .like(cat.getAdoptionStatus() != null, "adoption_status", cat.getAdoptionStatus());
//        System.out.println(qw+"-----------qw-----------");
        return catsMapper.selectList(qw);
    }

    @Override
    public List<Cats> listActive() {
        QueryWrapper<Cats> qw = new QueryWrapper<>();
        qw.eq("is_active", 1);
        return catsMapper.selectList(qw);
    }

    @Override
    public Page<Cats> getCatsList(Map<String, Object> requestParams) {
        // 获取分页参数
        int pageNum = (int) requestParams.getOrDefault("pageNum", 1);
        int pageSize = (int) requestParams.getOrDefault("pageSize", 10);
        int offset = (pageNum - 1) * pageSize;

        // 设置分页参数
        requestParams.put("offset", offset);
        requestParams.put("pageSize", pageSize);

        // 查询数据
        List<Cats> records = catsMapper.selectCatsNoNull(requestParams);

        // 查询总数
        int total = catsMapper.countCatsNoNull(requestParams);

        // 创建分页对象并设置数据
        Page<Cats> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(total);

        return page;
    }
}
