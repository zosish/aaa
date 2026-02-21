package com.zrrd.catcate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Roles;
import com.zrrd.catcate.service.IRolesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 角色表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-11-27
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcate/roles")
public class RolesController {
    @Resource
    private IRolesService rolesService;

    /**
     * 获取角色列表
     * @param requestParams
     * @return
     */
    @RequestMapping("/selectList")
    public Map<String,Object> selectList(Map<String,Object> requestParams){
        Page<Roles> page = rolesService.getRolesList(requestParams);
        Map<String,Object> response = new HashMap<>();
        response.put("total",page.getTotal());
        response.put("list",page.getRecords());
        return  response;
    }
}
