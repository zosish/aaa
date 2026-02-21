package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.CatHealthRecords;
import com.zrrd.catcate.entity.Cats;
import com.zrrd.catcate.service.ICatHealthRecordsService;
import com.zrrd.catcate.service.ICatsService;
import com.zrrd.catcate.service.impl.CatHealthRecordsServiceImpl;
import jakarta.annotation.Resource;
import jakarta.persistence.OrderColumn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 猫咪健康记录表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-11-24
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/catHealthRecords")
public class CatHealthRecordsController {
    @Resource
    private ICatHealthRecordsService catHealthRecordsService;
    @Resource
    private ICatsService catsService;
    /**
     * 获取猫咪健康记录列表（带搜索和分页）
     *
     * @param requestParams 搜索参数
     * @return 分页结果
     */
    @PostMapping("/selectList")
     public Map<String, Object> selectCatHealthRecordsList(@RequestBody Map<String, Object> requestParams){
        System.out.println("============================" + requestParams);
        // 执行分页查询
        Page<CatHealthRecords> page = catHealthRecordsService.getCatHealthRecordsList(requestParams);
        // 构造返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", page.getTotal());
        response.put("list", page.getRecords());

        return response;
     }

     /**
     * 添加或修改猫咪健康记录(同时修改cats表里的健康数据)
     *
     * @param catHealthRecords 猫咪健康记录对象
     * @return 是否成功
     */
     @PostMapping("/addOrUpdate")
    public boolean addOrUpdate(@RequestBody CatHealthRecords catHealthRecords){
        if(catHealthRecords.getCatId() != null){
            //修改（如果猫咪健康状态发生变化，cats表里的健康状态也一起发生变化）
            Cats cats = new Cats();
            cats.setId(catHealthRecords.getCatId());
            cats.setHealthStatus(catHealthRecords.getHealthStatus());
            catsService.updateById(cats);
        }
        return catHealthRecordsService.saveOrUpdate(catHealthRecords);
     }

     /**
     * 删除猫咪健康记录
     *
     * @param catHealthRecords 猫咪健康记录对象
     * @return 是否成功
     */
     @PostMapping("/deleteById")
    public boolean deleteById(@RequestBody CatHealthRecords catHealthRecords){
        return catHealthRecordsService.removeById(catHealthRecords);
     }

    /**
     * 批量删除
     */
    @PostMapping("/deleteByIds")
    public boolean deleteByIds(@RequestBody List<CatHealthRecords> catHealthRecords) {
        return catHealthRecordsService.removeByIds(catHealthRecords);
    }

}
