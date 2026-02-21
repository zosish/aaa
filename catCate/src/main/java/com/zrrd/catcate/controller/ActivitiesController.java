package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zrrd.catcate.entity.Activities;
import com.zrrd.catcate.service.IActivitiesService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 活动信息表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/activities")
public class ActivitiesController {
    @Resource
    private IActivitiesService activitiesService;

    // 文件上传目录
    @Value("${upload.path:./uploads/}")
    private String uploadPath;



    /**
     * 分页查询活动列表
     */
    @PostMapping("/selectList")
    public Map<String, Object> selectActivitiesList(@RequestBody Map<String, Object> requestParams) {
        System.out.println("============================" + requestParams);
        // 执行分页查询
        Page<Activities> page = activitiesService.getActivityList(requestParams);
        // 构造返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", page.getTotal());
        response.put("list", page.getRecords());

        return response;
    }

    /**
     * 修改是否推荐
     */
    @PostMapping("/updateFeatured")
    public boolean updateFeatured(@RequestBody Activities activities) {
        System.out.println("============================" + activities);
        return activitiesService.updateById(activities);
    }

    /**
     * 批量修改是否推荐
     */
    @PostMapping("/updateByIdFeatured")
    public boolean updateByIdFeatured(@RequestBody List<Activities> activities) {
        return activitiesService.updateBatchById(activities);
    }

    /**
     * 保存或修改活动
     */
    @PostMapping("/addOrUpdate")
    public boolean addOrUpdate(@RequestBody Activities activities) {
        return activitiesService.saveOrUpdate(activities);
    }

    /**
     * 删除活动
     */
    @PostMapping("/deleteById")
    public boolean deleteById(@RequestBody Activities activities) {
        return activitiesService.removeById(activities);
    }

    /**
     * 批量删除活动
     */
    @PostMapping("/deleteByIds")
    public boolean deleteByIds(@RequestBody List<Activities> activities) {
        return activitiesService.removeByIds(activities);
    }

    /**
     * 修改活动状态
     */
    @PostMapping("/updateStatus")
    public boolean updateStatus(@RequestBody Activities activities) {
        System.out.println("============================" + activities);
        return activitiesService.updateById(activities);
    }

    /**
     * 批量修改活动状态
     */
    @PostMapping("/updateByIdStatus")
    public boolean updateByIdStatus(@RequestBody List<Activities> activities) {
        return activitiesService.updateBatchById(activities);
    }
    /**
     * 活动图片上传接口
     *
     * @param file 上传的文件
     * @param request HTTP请求对象
     * @return 上传结果
     */
    @PostMapping("/uploadImage")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        System.out.println("=== 活动图片上传请求开始 ===");
        System.out.println("接收到文件上传请求");
        System.out.println("文件名: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize() + " bytes");
        System.out.println("文件类型: " + file.getContentType());

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

        // 检查文件大小（限制为5MB）
        long maxSize = 8 * 1024 * 1024; // 8MB
        if (file.getSize() > maxSize) {
            result.put("code", 400);
            result.put("message", "图片大小不能超过8MB，当前文件大小: " + (file.getSize() / 1024 / 1024) + "MB");
            System.out.println("文件过大: " + file.getSize() + " bytes");
            return result;
        }

        try {
            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath + "activities/");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("创建上传目录: " + uploadDir.toAbsolutePath());
            }

            // 获取原始文件名并生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            System.out.println("生成文件名: " + uniqueFileName);

            // 保存文件
            Path filePath = uploadDir.resolve(uniqueFileName);
            file.transferTo(filePath);
            System.out.println("文件保存路径: " + filePath.toAbsolutePath());

            // 构造完整访问URL（使用当前请求的协议和端口）
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();

            String fileUrl = scheme + "://" + serverName + ":" + serverPort + "/uploads/activities/" + uniqueFileName;
            System.out.println("文件访问URL: " + fileUrl);

            // 返回结果
            result.put("code", 200);
            result.put("message", "上传成功");
            Map<String, String> data = new HashMap<>();
            data.put("url", fileUrl);
            result.put("data", data);

            System.out.println("=== 活动图片上传成功 ===");

        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "文件上传失败: " + e.getMessage());
            System.err.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}