package com.zrrd.catcate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrrd.catcate.entity.Products;
import com.zrrd.catcate.mapper.ProductsMapper;
import com.zrrd.catcate.service.IProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品信息表 服务实现类
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements IProductsService {
    @Resource
    private ProductsMapper productsMapper;

//    @Override
//    public Page<Products> getProductList(Map<String, Object> requestParams) {
//        System.out.println("---------------------------"+requestParams);
//        // 获取分页参数
//        int pageNum = (int) requestParams.getOrDefault("pageNum", 1);
//        int pageSize = (int) requestParams.getOrDefault("pageSize", 10);
//
//        // 创建分页对象
//        Page<Products> page = new Page<>(pageNum, pageSize);
//        System.out.println("===================" + page);
//
//        List<Products> productsPage = productsMapper.selectProductsNoNull(requestParams);
//        System.out.println("===================" + productsPage);
//        // 执行分页查询
//        return page.setRecords(productsPage);
//    }
    @Override
    public Page<Products> getProductList(Map<String, Object> requestParams) {
        // 获取分页参数
        int pageNum = (int) requestParams.getOrDefault("pageNum", 1);
        int pageSize = (int) requestParams.getOrDefault("pageSize", 10);
        int offset = (pageNum - 1) * pageSize;

        // 设置分页参数
        requestParams.put("offset", offset);
        requestParams.put("pageSize", pageSize);

        // 查询数据
        List<Products> records = productsMapper.selectProductsNoNull(requestParams);

        // 查询总数
        int total = productsMapper.countProductsNoNull(requestParams);

        // 创建分页对象并设置数据
        Page<Products> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(total);

        return page;
    }

}
