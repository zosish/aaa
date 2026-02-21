package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.ProductCategories;
import com.zrrd.catcate.service.IProductCategoriesService;
import jakarta.annotation.Resource;
import jakarta.persistence.OrderColumn;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
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
    @PostMapping("/selectList")
    public Map<String, Object> selectList(@RequestBody Map<String, Object> params) {
        Page<ProductCategories> page = productCategoriesService.getProductCategoriesList(params);
        Map<String , Object> response = new HashMap<>();
        response.put("total", page.getTotal());
        response.put("list",page.getRecords());
        return response;
    }
}
