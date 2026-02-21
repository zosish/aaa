package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.Roles;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-11-27
 */
public interface RolesMapper extends BaseMapper<Roles> {
    List<Roles> selectRolesNoNull(Map<String, Object> params);
    int countRolesNoNull(Map<String, Object> params);
}
