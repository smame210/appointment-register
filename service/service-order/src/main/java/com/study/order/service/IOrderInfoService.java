package com.study.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.study.model.order.OrderInfo;
import com.study.vo.order.OrderCountQueryVo;
import com.study.vo.order.OrderQueryVo;

import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author smame210
 * @since 2021-09-09
 */
public interface IOrderInfoService extends IService<OrderInfo> {

    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);

    Long saveOrder(String scheduleId, Long patientId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    OrderInfo  getOrder(String id);

    Map<String, Object> show(Long id);

    Boolean cancelOrder(Long orderId);

    /**
     * 就诊提醒
     */
    void patientTips();
}
