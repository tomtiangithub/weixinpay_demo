package com.meihong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meihong.entity.OrderInfo;

import java.util.List;

public interface OrderInfoService extends IService<OrderInfo> {
    OrderInfo createOrderByProductId(Long productId);
    void saveCodeUrl(String orderNo,String codeUrl);
    List<OrderInfo> listOrderByCreateTimeDesc();
}
