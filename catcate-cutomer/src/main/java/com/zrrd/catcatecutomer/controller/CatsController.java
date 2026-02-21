package com.zrrd.catcatecutomer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcatecutomer.entity.Cats;
import com.zrrd.catcatecutomer.service.ICatsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 猫咪信息表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/cats")
public class CatsController {

    @Resource
    private ICatsService catsService;

    /**
     * 分页查询猫咪列表
     */
    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody Map<String, Object> params) {
        try {
            Page<Cats> page = catsService.getCatsList(params);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("total", page.getTotal());
            response.put("records", page.getRecords());
            response.put("list", page.getRecords());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 获取预约次数最多的热门猫咪
     */
    @GetMapping("/popular")
    public Map<String, Object> getPopularCats(@RequestParam(defaultValue = "4") int limit) {
        try {
            List<Cats> cats = catsService.getPopularCats(limit);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", cats);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 获取猫咪详情（预约页面）
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getCatDetail(@PathVariable Long id) {
        try {
            Cats cat = catsService.getById(id);
            Map<String, Object> response = new HashMap<>();
            if (cat != null) {
                response.put("code", 200);
                response.put("message", "查询成功");
                response.put("data", cat);
            } else {
                response.put("code", 404);
                response.put("message", "猫咪不存在");
            }
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "查询失败：" + e.getMessage());
            return response;
        }
    }

    /**
     * 上传猫咪照片
     */
    @PostMapping("/uploadPhoto")
    public Map<String, Object> uploadPhoto(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        System.out.println("=== 接收到猫咪图片上传请求 ===");
        System.out.println("文件名: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize() + " bytes");
        System.out.println("文件类型: " + file.getContentType());

        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "上传文件不能为空");
                System.out.println("文件为空");
                return result;
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("code", 400);
                result.put("message", "只允许上传图片文件");
                System.out.println("文件类型不正确: " + contentType);
                return result;
            }

            // 检查文件大小（限制为8MB）
            long maxSize = 8 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                result.put("code", 400);
                result.put("message", "图片大小不能超过8MB，当前文件大小: " + (file.getSize() / 1024 / 1024) + "MB");
                System.out.println("文件过大: " + file.getSize() + " bytes");
                return result;
            }

            // 创建上传目录 - 使用项目根目录的绝对路径
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + "/uploads/cats/";
            File dir = new File(uploadDir);
            System.out.println("项目路径: " + projectPath);
            System.out.println("上传目录路径: " + dir.getAbsolutePath());

            if (!dir.exists()) {
                System.out.println("目录不存在，尝试创建...");
                boolean created = dir.mkdirs();
                System.out.println("创建目录结果: " + created);
                if (!created) {
                    System.out.println("目录创建失败");
                    result.put("code", 500);
                    result.put("message", "无法创建上传目录: " + dir.getAbsolutePath());
                    return result;
                }
            }

            // 检查目录是否可写
            if (!dir.canWrite()) {
                System.out.println("目录不可写");
                result.put("code", 500);
                result.put("message", "上传目录不可写: " + dir.getAbsolutePath());
                return result;
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;

            System.out.println("新文件名: " + newFilename);

            // 保存文件
            File dest = new File(dir, newFilename);
            System.out.println("目标文件路径: " + dest.getAbsolutePath());

            // 确保父目录存在
            File parentDir = dest.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            file.transferTo(dest);
            System.out.println("文件保存路径: " + dest.getAbsolutePath());
            System.out.println("文件保存成功");

            // 返回访问URL（相对路径）
            String fileUrl = "/uploads/cats/" + newFilename;
            System.out.println("返回URL: " + fileUrl);

            result.put("code", 200);
            result.put("message", "上传成功");
            result.put("data", Map.of(
                    "url", fileUrl,
                    "filename", newFilename
            ));

            System.out.println("=== 文件上传请求结束 ===");

        } catch (IOException e) {
            System.err.println("文件IO异常:");
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("其他异常:");
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "服务器内部错误: " + e.getMessage());
        }

        return result;
    }
}