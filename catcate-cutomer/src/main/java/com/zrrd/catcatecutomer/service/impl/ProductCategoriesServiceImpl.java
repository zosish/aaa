package com.zrrd.catcatecutomer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zrrd.catcatecutomer.entity.ProductCategories;
import com.zrrd.catcatecutomer.mapper.ProductCategoriesMapper;
import com.zrrd.catcatecutomer.service.IProductCategoriesService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 商品分类表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Service
public class ProductCategoriesServiceImpl extends ServiceImpl<ProductCategoriesMapper, ProductCategories> implements IProductCategoriesService {

    @Override
    public List<ProductCategories> getActiveCategories() {
        return this.baseMapper.selectActiveCategories();
    }

    @Override
    public boolean save(ProductCategories entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return super.save(entity);
    }

    @Override
    public boolean updateById(ProductCategories entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return super.updateById(entity);
    }
}