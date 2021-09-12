package com.study.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.model.order.OrderInfo;
import com.study.vo.order.OrderCountQueryVo;
import com.study.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author smame210
 * @since 2021-09-09
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    List<OrderCountVo> selectOrderCount(@Param("vo")OrderCountQueryVo orderCountQueryVo);
}
