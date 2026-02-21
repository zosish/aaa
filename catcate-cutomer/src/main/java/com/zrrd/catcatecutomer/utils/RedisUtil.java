package com.zrrd.catcatecutomer.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置用户ID缓存
     * @param token 用户标识token
     * @param userId 用户ID
     * @param timeout 过期时间(秒)
     */
    public void setUserCache(String token, Long userId, long timeout) {
        redisTemplate.opsForValue().set("user:token:" + token, userId, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取用户ID
     * @param token 用户标识token
     * @return 用户ID
     */
    public Long getUserIdByToken(String token) {
        Object userId = redisTemplate.opsForValue().get("user:token:" + token);
        if (userId != null) {
            // 处理不同类型的返回值
            if (userId instanceof Long) {
                return (Long) userId;
            } else if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof String) {
                try {
                    return Long.valueOf((String) userId);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 删除用户缓存
     * @param token 用户标识token
     */
    public void deleteUserCache(String token) {
        redisTemplate.delete("user:token:" + token);
    }

    /**
     * 检查token是否存在
     * @param token 用户标识token
     * @return 是否存在
     */
    public boolean hasToken(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("user:token:" + token));
    }
}