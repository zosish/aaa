package com.zrrd.catcatecutomer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zrrd.catcatecutomer.entity.Favorites;

import java.util.List;

public interface IFavoritesService extends IService<Favorites> {

    /**
     * 获取用户收藏列表
     */
    List<Favorites> getUserFavorites(Long userId);

    /**
     * 检查是否已收藏
     */
    boolean isFavoriteExists(Long userId, String targetType, Long targetId);

    /**
     * 取消收藏
     */
    boolean removeFavorite(Long userId, String targetType, Long targetId);
}