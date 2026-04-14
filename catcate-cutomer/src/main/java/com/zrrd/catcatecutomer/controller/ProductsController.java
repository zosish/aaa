package com.zrrd.catcatecutomer.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcatecutomer.entity.ProductCategories;
import com.zrrd.catcatecutomer.entity.Products;
import com.zrrd.catcatecutomer.service.IProductCategoriesService;
import com.zrrd.catcatecutomer.service.IProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品信息表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductsController {

    @Autowired
    private IProductsService productsService;

    @Autowired
    private IProductCategoriesService productCategoriesService;

    /**
     * 获取商品分类列表
     * @return 分类列表
     */
    @GetMapping("/list")
    public Map<String, Object> getCategories() {
        System.out.println("=== 访问商品分类接口 ===");
        Map<String, Object> result = new HashMap<>();
        try {
            List<ProductCategories> categories = productCategoriesService.getActiveCategories();
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
     * 分页获取商品列表
     * @param params 查询参数
     * @return 商品列表
     */
    @PostMapping("/list")
    public Map<String, Object> getProductList(@RequestBody Map<String, Object> params) {
        System.out.println("=== 访问商品列表接口 ===");
        System.out.println("请求参数: " + params);
        Map<String, Object> result = new HashMap<>();
        try {
            Page<Products> page = productsService.getProductPage(params);
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", page);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取销售数量最多的热门商品
     */
    @GetMapping("/best-selling")
    public Map<String, Object> getBestSellingProducts(@RequestParam(defaultValue = "4") int limit) {
        try {
            List<Products> products = productsService.getBestSellingProducts(limit);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", products);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getProductDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Products product = productsService.getProductById(id);
            if (product != null && product.getIsAvailable() == 1) {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", product);
            } else {
                result.put("code", 404);
                result.put("message", "商品不存在或已下架");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败：" + e.getMessage());
        }
        return result;
    }

//    /**
//     * 添加商品到购物车
//     * @param request 请求对象
//     * @param productId 商品ID
//     * @param quantity 数量
//     * @return 结果
//     */
//    @PostMapping("/cart/add")
//    public Map<String, Object> addToCart(HttpServletRequest request,
//                                         @RequestParam Long productId,
//                                         @RequestParam(defaultValue = "1") Integer quantity) {
//        Map<String, Object> result = new HashMap<>();
//
//        // 验证用户登录
//        String token = request.getHeader("Authorization");
//        if (token == null || !JwtHelper.verifyToken(token)) {
//            result.put("code", 401);
//            result.put("message", "请先登录");
//            return result;
//        }
//
//        try {
//            Products product = productsService.getProductById(productId);
//            if (product == null) {
//                result.put("code", 404);
//                result.put("message", "商品不存在");
//                return result;
//            }
//
//            if (product.getIsAvailable() != 1) {
//                result.put("code", 400);
//                result.put("message", "商品已下架");
//                return result;
//            }
//
//            if (product.getStockQuantity() < quantity) {
//                result.put("code", 400);
//                result.put("message", "库存不足");
//                return result;
//            }
//
//            // 获取用户ID
//            Long userId = JwtHelper.getUserIdStatic(token);
//            if (userId == null) {
//                result.put("code", 401);
//                result.put("message", "用户信息无效");
//                return result;
//            }
//
//            // 这里应该调用购物车服务，这里简化处理
//            result.put("code", 200);
//            result.put("message", "添加成功");
//
//        } catch (Exception e) {
//            result.put("code", 500);
//            result.put("message", "添加失败：" + e.getMessage());
//        }
//
//        return result;
//    }
}