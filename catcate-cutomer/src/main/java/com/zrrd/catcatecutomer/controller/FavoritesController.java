package com.zrrd.catcatecutomer.controller;

import com.zrrd.catcatecutomer.entity.Favorites;
import com.zrrd.catcatecutomer.service.IFavoritesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 收藏表 前端控制器
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@RestController
@CrossOrigin(value = "*")
@RequestMapping("/catcatecutomer/favorites")
public class FavoritesController {

    @Resource
    private IFavoritesService favoritesService;

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserFavorites(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Favorites> favorites = favoritesService.getUserFavorites(userId);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", favorites);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取用户收藏异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 添加收藏
     */
    @PostMapping
    public Map<String, Object> addFavorite(@RequestBody Favorites favorite) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查是否已收藏
            boolean exists = favoritesService.isFavoriteExists(favorite.getUserId(),
                    favorite.getTargetType(),
                    favorite.getTargetId());

            if (exists) {
                result.put("code", 400);
                result.put("message", "已收藏该商品");
                return result;
            }

            favorite.setCreateTime(LocalDateTime.now());

            boolean success = favoritesService.save(favorite);

            if (success) {
                result.put("code", 200);
                result.put("message", "收藏成功");
                result.put("data", favorite);
            } else {
                result.put("code", 500);
                result.put("message", "收藏失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加收藏异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 取消收藏
     */
    @DeleteMapping
    public Map<String, Object> removeFavorite(@RequestParam Long userId,
                                              @RequestParam String targetType,
                                              @RequestParam Long targetId) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = favoritesService.removeFavorite(userId, targetType, targetId);

            if (success) {
                result.put("code", 200);
                result.put("message", "取消收藏成功");
            } else {
                result.put("code", 500);
                result.put("message", "取消收藏失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "取消收藏异常：" + e.getMessage());
        }

        return result;
    }
}