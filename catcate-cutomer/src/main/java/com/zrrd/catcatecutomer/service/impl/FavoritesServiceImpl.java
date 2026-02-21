package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.Favorites;
import com.zrrd.catcatecutomer.mapper.FavoritesMapper;
import com.zrrd.catcatecutomer.service.IFavoritesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements IFavoritesService {

    @Override
    public List<Favorites> getUserFavorites(Long userId) {
        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public boolean isFavoriteExists(Long userId, String targetType, Long targetId) {
        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("target_type", targetType)
                .eq("target_id", targetId);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, String targetType, Long targetId) {
        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("target_type", targetType)
                .eq("target_id", targetId);
        return this.remove(queryWrapper);
    }
}