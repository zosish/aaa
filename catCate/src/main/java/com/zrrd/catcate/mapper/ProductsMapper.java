package com.zrrd.catcate.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Products;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品信息表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Mapper
public interface ProductsMapper extends BaseMapper<Products> {
    /**
     * 获取商品列表（带搜索和分页）
     *
     * @param   products 搜索参数
     * @return 分页结果
     */
    List<Products> selectProductsNoNull(Map<String, Object> products);

    int countProductsNoNull(Map<String, Object> params);

}
