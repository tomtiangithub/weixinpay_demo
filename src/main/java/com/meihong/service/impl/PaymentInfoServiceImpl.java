package com.meihong.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.meihong.entity.PaymentInfo;
import com.meihong.enums.PayType;
import com.meihong.mapper.PaymentInfoMapper;
import com.meihong.service.PaymentInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    @Override
    public void createPaymentInfo(String plainText) {
        log.info("记录支付日志");
        Gson gson = new Gson();
        Map plainTextMap = gson.fromJson(plainText, HashMap.class);
        //商户订单号
        String orderNo= (String) plainTextMap.get("out_trade_no");
        //微信支付订单号
        String transactionId= (String) plainTextMap.get("transaction_id");
        //交易类型
        String tradeType= (String) plainTextMap.get("trade_type");
        //交易状态
        String tradeState= (String) plainTextMap.get("trade_state");
        //订单金额	amount用户支付金额	payer_total
        Map<String,Object> amount= (Map) plainTextMap.get("amount");
        Integer payerTotal= ((Double) amount.get("payer_total")).intValue();

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(payerTotal);
        paymentInfo.setContent(plainText);

        baseMapper.insert(paymentInfo);

    }
}
