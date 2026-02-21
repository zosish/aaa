// 修改 com.zrrd.catcate.mapper.OrderItemsMapper.java
package com.zrrd.catcate.mapper;

import com.zrrd.catcate.entity.OrderItems;
import com.zrrd.catcate.entity.VO.UserProductVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 订单商品明细表 Mapper 接口
 * </p>
 *
 * @author xyd
 * @since 2025-10-22
 */
@Mapper
public interface OrderItemsMapper extends BaseMapper<OrderItems> {
    /**
     * 查询所有用户购买的商品信息
     * @return
     */
    List<UserProductVO> selectAllUserProductInfo();
}
