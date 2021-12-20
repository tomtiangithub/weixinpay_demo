package com.meihong.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meihong.entity.OrderInfo;
import com.meihong.mapper.OrderInfoMapper;
import com.meihong.service.OrderInfoService;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

}
