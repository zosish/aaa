package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.ProductCategories;
import com.zrrd.catcate.service.IProductCategoriesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品分类表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-11-26
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/productCategories")
public class ProductCategoriesController {
    @Resource
    private IProductCategoriesService productCategoriesService;
    
    /**
     * 获取分类列表
     */
    @PostMapping("/selectList")
    public Map<String, Object> selectList(@RequestBody Map<String, Object> params) {
        Page<ProductCategories> page = productCategoriesService.getProductCategoriesList(params);
        Map<String , Object> response = new HashMap<>();
        response.put("total", page.getTotal());
        response.put("list",page.getRecords());
        return response;
    }
    
    /**
     * 检查分类代码唯一性
     */
    @GetMapping("/checkCodeUnique")
    public Map<String, Object> checkCodeUnique(@RequestParam String code, @RequestParam(required = false) Long id) {
        boolean isUnique = productCategoriesService.checkCodeUnique(code, id);
        Map<String, Object> response = new HashMap<>();
        response.put("isUnique", isUnique);
        return response;
    }
    
    /**
     * 获取父分类选项（一级分类）
     */
    @GetMapping("/selectParentOptions")
    public List<ProductCategories> selectParentOptions() {
        return productCategoriesService.getParentCategoryOptions();
    }
    
    /**
     * 新增或编辑分类
     */
    @PostMapping("/addOrUpdate")
    public Map<String, Object> addOrUpdate(@RequestBody ProductCategories category) {
        boolean success;
        if (category.getId() == null) {
            success = productCategoriesService.save(category);
        } else {
            success = productCategoriesService.updateById(category);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "操作成功" : "操作失败");
        return response;
    }
    
    /**
     * 删除分类
     */
    @PostMapping("/deleteById")
    public Map<String, Object> deleteById(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        boolean success = productCategoriesService.removeById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "删除成功" : "删除失败");
        return response;
    }
    
    /**
     * 批量删除分类
     */
    @PostMapping("/deleteByIds")
    public Map<String, Object> deleteByIds(@RequestBody Map<String, Object> params) {
        List<Long> ids = (List<Long>) params.get("ids");
        boolean success = productCategoriesService.deleteByIds(ids);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "批量删除成功" : "批量删除失败");
        return response;
    }
    
    /**
     * 更新分类排序
     */
    @PostMapping("/updateSort")
    public Map<String, Object> updateSort(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer sortOrder = Integer.valueOf(params.get("sortOrder").toString());
        boolean success = productCategoriesService.updateSort(id, sortOrder);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "排序更新成功" : "排序更新失败");
        return response;
    }
    
    /**
     * 更新分类状态
     */
    @PostMapping("/updateStatus")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Byte isActive = Byte.valueOf(params.get("isActive").toString());
        boolean success = productCategoriesService.updateStatus(id, isActive);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "状态更新成功" : "状态更新失败");
        return response;
    }
}