package com.zrrd.catcate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.ProductCategories;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品分类表 服务类
 * </p>
 *
 * @author xyd
 * @since 2025-11-26
 */
public interface IProductCategoriesService extends IService<ProductCategories> {
    Page<ProductCategories> getProductCategoriesList(Map<String, Object> requestParams);
    
    /**
     * 检查分类代码是否唯一
     */
    boolean checkCodeUnique(String code, Long id);
    
    /**
     * 获取父分类选项（一级分类）
     */
    List<ProductCategories> getParentCategoryOptions();
    
    /**
     * 更新分类排序
     */
    boolean updateSort(Long id, Integer sortOrder);
    
    /**
     * 更新分类状态
     */
    boolean updateStatus(Long id, Byte isActive);
    
    /**
     * 批量删除分类
     */
    boolean deleteByIds(List<Long> ids);
}
