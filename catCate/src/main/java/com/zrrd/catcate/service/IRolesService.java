package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Roles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-11-27
 */
public interface IRolesService extends IService<Roles> {
    Page<Roles> getRolesList(Map<String, Object> params);
}
