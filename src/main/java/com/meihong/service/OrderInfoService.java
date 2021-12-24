package com.meihong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meihong.entity.OrderInfo;
import com.meihong.enums.OrderStatus;

import java.util.List;

public interface OrderInfoService extends IService<OrderInfo> {
    OrderInfo createOrderByProductId(Long productId);
    void saveCodeUrl(String orderNo,String codeUrl);
    List<OrderInfo> listOrderByCreateTimeDesc();
    void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus);
    String getOrderStatus(String orderNo);

    List<OrderInfo> getNoPayOrderByDuration(int minute);
}
