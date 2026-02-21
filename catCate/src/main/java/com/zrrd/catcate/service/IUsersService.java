package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
public interface IUsersService extends IService<Users> {
    Integer register(Users users);
    Boolean login(Users users);
    List<Object> stats();
    List<Users> selectList(Users users);
    Page<Users> getUsersList(Map<String, Object> requestParams);
    /**
     * 获取最近动态数据
     * @return 最近动态列表
     */
    List<Map<String, Object>> getRecentActivities();
}
