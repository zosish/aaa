package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Products;
import com.zrrd.catcate.service.IProductsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 商品信息表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/products")
public class ProductsController {
    @Autowired
    private IProductsService productsService;

    // 文件上传目录
    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    /**
     * 获取商品列表（带搜索和分页）
     *
     * @param requestParams 搜索参数
     * @return 分页结果
     */
    @PostMapping("/selectList")
    public Map<String, Object> selectProductList(@RequestBody Map<String, Object> requestParams) {
        System.out.println("============================" + requestParams);
        // 执行分页查询
        Page<Products> resultPage = productsService.getProductList(requestParams);

        // 构造返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", resultPage.getTotal());
        response.put("list", resultPage.getRecords());

        return response;
    }

    /**
     * 添加或更新商品
     */
    @PostMapping("addOrUpdate")
    public List<Products> addOrUpdate(@RequestBody Products products) {
        System.out.println("============================" + products);
        productsService.saveOrUpdate(products);
        return productsService.list();
    }

    /**
     * 根据id删除商品
     */
    @PostMapping("/deleteById")
    public boolean deleteById(@RequestBody Products products) {
        System.out.println("============================" + products);
        return productsService.removeById(products);
    }

    /**
     * 批量删除
     */
    @PostMapping("/deleteByIds")
    public boolean deleteByIds(@RequestBody List<Products> products) {
        System.out.println("============================" + products);
        return productsService.removeByIds(products);
    }

    /**
     * 单个修改上下架状态
     */
    @PostMapping("/updateStatus")
    public boolean updateStatus(@RequestBody Products products) {
        System.out.println("============================" + products);
        return productsService.updateById(products);
    }

    /**
     * 批量修改上下架状态
     */
    @PostMapping("/updateByIdStatus")
    public boolean updateByIdStatus(@RequestBody List<Products> products) {
        System.out.println("============================" + products);
        return productsService.updateBatchById(products);
    }

    /**
     * 图片上传接口
     *
     * @param file 上传的文件
     * @return 上传结果
     */
    @PostMapping("/uploadImage")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 检查文件是否为空
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("message", "上传文件不能为空");
            return result;
        }

        try {
            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 获取原始文件名并生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(uniqueFileName);
            file.transferTo(filePath);

            // 构造完整访问URL（使用当前请求的协议和端口）
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();

            String fileUrl = scheme + "://" + serverName + ":" + serverPort + "/uploads/" + uniqueFileName;

            // 返回结果
            result.put("code", 200);
            result.put("message", "上传成功");
            Map<String, String> data = new HashMap<>();
            data.put("url", fileUrl);
            result.put("data", data);

        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "文件上传失败: " + e.getMessage());
        }

        return result;
    }

}
