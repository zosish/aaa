package com.zrrd.catcatecutomer.controller;


import com.zrrd.catcatecutomer.entity.ProductCategories;
import com.zrrd.catcatecutomer.service.IProductCategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @since 2025-10-29
 */
@RestController
@RequestMapping("/catcatecutomer/product-categories")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductCategoriesController {

    @Autowired
    private IProductCategoriesService productCategoriesService;

    /**
     * 获取所有启用的分类
     * @return 分类列表
     */
    @GetMapping("/list")
    public Map<String, Object> getCategoryList() {
        System.out.println("=== 访问分类列表接口 ===");
        Map<String, Object> result = new HashMap<>();
        try {
            List<ProductCategories> categories = productCategoriesService.list();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", categories);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 添加分类
     * @param category 分类信息
     * @return 结果
     */
    @PostMapping("/add")
    public Map<String, Object> addCategory(@RequestBody ProductCategories category) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = productCategoriesService.save(category);
            if (success) {
                result.put("code", 200);
                result.put("message", "添加成功");
            } else {
                result.put("code", 500);
                result.put("message", "添加失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新分类
     * @param category 分类信息
     * @return 结果
     */
    @PutMapping("/update")
    public Map<String, Object> updateCategory(@RequestBody ProductCategories category) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = productCategoriesService.updateById(category);
            if (success) {
                result.put("code", 200);
                result.put("message", "更新成功");
            } else {
                result.put("code", 500);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 结果
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteCategory(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = productCategoriesService.removeById(id);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除成功");
            } else {
                result.put("code", 500);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }
}