package com.meihong.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meihong.entity.PaymentInfo;
import com.meihong.mapper.PaymentInfoMapper;
import com.meihong.service.PaymentInfoService;
import org.springframework.stereotype.Service;

@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

}
