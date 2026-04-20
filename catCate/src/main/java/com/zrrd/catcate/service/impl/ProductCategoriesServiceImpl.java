package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.ProductCategories;
import com.zrrd.catcate.mapper.ProductCategoriesMapper;
import com.zrrd.catcate.service.IProductCategoriesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品分类表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-11-26
 */
@Service
public class ProductCategoriesServiceImpl extends ServiceImpl<ProductCategoriesMapper, ProductCategories> implements IProductCategoriesService {
    @Resource
    private ProductCategoriesMapper productCategoriesMapper;

    @Override
    public Page<ProductCategories> getProductCategoriesList(Map<String, Object> requestParams) {
        int pageNum = (int)requestParams.getOrDefault("pageNum", 1);
        int pageSize = (int)requestParams.getOrDefault("pageSize", 10);
        int offset = (pageNum - 1) * pageSize;
        requestParams.put("offset", offset);
        requestParams.put("pageSize", pageSize);
        List<ProductCategories> records = productCategoriesMapper.selectProductCategoriesNoNull(requestParams);
        int total = productCategoriesMapper.countProductCategoriesNoNull(requestParams);
        Page<ProductCategories> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public boolean checkCodeUnique(String code, Long id) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("code", code);
        if (id != null) {
            params.put("notId", id);
        }
        int count = productCategoriesMapper.countByCode(params);
        return count == 0;
    }

    @Override
    public List<ProductCategories> getParentCategoryOptions() {
        return productCategoriesMapper.selectParentCategories();
    }

    @Override
    public boolean updateSort(Long id, Integer sortOrder) {
        ProductCategories category = new ProductCategories();
        category.setId(id);
        category.setSortOrder(sortOrder);
        return updateById(category);
    }

    @Override
    public boolean updateStatus(Long id, Byte isActive) {
        ProductCategories category = new ProductCategories();
        category.setId(id);
        category.setIsActive(isActive);
        return updateById(category);
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }
}