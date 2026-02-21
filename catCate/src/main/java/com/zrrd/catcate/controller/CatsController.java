package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Cats;
import com.zrrd.catcate.service.ICatsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
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
 * 猫咪信息表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/cats")
public class CatsController {
    @Resource
    private ICatsService catsService;
    @Resource
    private Cats cats;

    // 文件上传基础路径
    private static final String UPLOAD_DIR = "uploads/cats/";

    /**
     * 上传猫咪照片
     */
    @PostMapping("/uploadPhoto")
    public Map<String, Object> uploadPhoto(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        System.out.println("=== 文件上传请求开始 ===");
        System.out.println("接收到文件上传请求");
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
            long maxSize = 8 * 1024 * 1024; // 8MB
            if (file.getSize() > maxSize) {
                result.put("code", 400);
                result.put("message", "图片大小不能超过8MB，当前文件大小: " + (file.getSize() / 1024 / 1024) + "MB");
                System.out.println("文件过大: " + file.getSize() + " bytes");
                return result;
            }

            // 创建上传目录 - 使用绝对路径
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

            // 返回访问URL
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

//    @PostMapping("/list")
//    public List<Cats> list() {
//        return catsService.list();
//    }

    /**
     * 分页查询猫咪
     *
     * @param params
     * @return
     */
    @PostMapping("/list")
    public Page<Cats> list(@RequestBody Map<String, Object> params) {
        System.out.println("============================" + params);
        Page<Cats> page = catsService.getCatsList(params);
        Map<String, Object> map = new HashMap<>();
        map.put("total", page.getTotal());
        map.put("list", page.getRecords());
        return page;
    }
    /**
     * 获取所有猫咪
     */
    @GetMapping("/listAll")
    public List<Cats> listAll() {
        return catsService.list();
    }

    /**
     * 条件查询（好像没用到，用上边的list方法里了）
     *
     * @param cat
     */
    @PostMapping("/conditionList")
    public List<Cats> conditionList(@RequestBody Cats cat) {
        return catsService.conditionList(cat);
    }

    /**
     * 修改猫咪是否活动状态
     *
     * @param params
     */
    @PostMapping("/changeActive")
    public boolean changeActive(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String isActive = (String) params.get("isActive");
        cats.setId(Long.valueOf(id));
        cats.setIsActive(Byte.valueOf(isActive));
        return catsService.updateById(cats);
    }

    /**
     * 添加或修改
     *
     * @param cat
     */
    @PostMapping("/addOrUpdate")
    public boolean addOrUpdate(@RequestBody Cats cat) {
        System.out.println(cat);
//        如果有id就修改，如果没有id就保存
        return catsService.saveOrUpdate(cat);
    }

    /**
     * 删除猫咪
     *
     * @param cat
     */
    @PostMapping("/deleteCat")
    public boolean deleteCat(@RequestBody Cats cat) {
        System.out.println(cat);
        return catsService.removeById(cat);
    }

    /**
     * 批量删除
     */
    @PostMapping("/deleteByIds")
    public boolean deleteByIds(@RequestBody List<Cats> cats){
        return catsService.removeByIds(cats);
    }

    /**
     * 获取所有活动猫咪
     */
    @GetMapping("/listActive")
    public List<Cats> listActive() {
        return catsService.listActive();
    }
}
