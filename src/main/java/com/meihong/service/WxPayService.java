package com.meihong.service;

import java.io.IOException;
import java.util.Map;

public interface WxPayService {
    Map<String, Object> nativePay(Long productId) throws Exception;
    void processOrder(Map<String, Object> bodyMap) throws Exception;
    void cancelOrder(String orderNo) throws Exception;
    String queryOrder(String orderNo) throws Exception;
}
