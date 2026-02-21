package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Roles;
import com.zrrd.catcate.mapper.RolesMapper;
import com.zrrd.catcate.service.IRolesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-11-27
 */
@Service
public class RolesServiceImpl extends ServiceImpl<RolesMapper, Roles> implements IRolesService {

    @Override
    public Page<Roles> getRolesList(Map<String, Object> params) {
        return null;
    }
}
