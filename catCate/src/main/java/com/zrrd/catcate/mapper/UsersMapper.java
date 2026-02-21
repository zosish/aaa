package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    List<Users> selectUsersNoNull(Map<String,Object>  params);
    int countUsersNoNull(Map<String,Object>  params);
}
