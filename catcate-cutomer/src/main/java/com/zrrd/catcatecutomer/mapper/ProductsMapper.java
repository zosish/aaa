package com.zrrd.catcatecutomer.mapper;

import com.zrrd.catcatecutomer.entity.Products;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 商品信息表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-29
 */
@Mapper
public interface ProductsMapper extends BaseMapper<Products> {

    /**
     * 更新商品库存和销量（带乐观锁）
     * @param productId 商品ID
     * @param reduceQuantity 减少的库存数量
     * @param increaseSales 增加的销量
     * @param id 商品ID（用于where条件）
     * @param requiredStock 需要的库存数量（用于库存检查）
     * @return 影响的行数
     */
    @Update("UPDATE products SET " +
            "stock_quantity = stock_quantity - #{reduceQuantity}, " +
            "sales_count = sales_count + #{increaseSales}, " +
            "update_time = NOW() " +
            "WHERE id = #{id} AND stock_quantity >= #{requiredStock}")
    int updateStockAndSales(@Param("id") Long productId,
                            @Param("reduceQuantity") Integer reduceQuantity,
                            @Param("increaseSales") Integer increaseSales,
                            @Param("requiredStock") Integer requiredStock);

    /**
     * 获取销售数量最多的热门商品
     * @param limit 返回数量
     * @return 热门商品列表
     */
    List<Products> getBestSellingProducts(int limit);
}