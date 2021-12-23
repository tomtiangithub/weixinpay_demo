package com.meihong.service.impl;

import com.google.gson.Gson;
import com.meihong.config.WxPayConfig;
import com.meihong.entity.OrderInfo;
import com.meihong.enums.OrderStatus;
import com.meihong.enums.wxpay.WxApiType;
import com.meihong.enums.wxpay.WxNotifyType;
import com.meihong.service.OrderInfoService;
import com.meihong.service.WxPayService;
import com.meihong.util.OrderNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private CloseableHttpClient wxPayClient;

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 创建订单，调用native 支付接口
     *
     * @param productId
     * @return code_url
     * @throws Exception
     */
    @Override
    public Map<String, Object> nativePay(Long productId) throws Exception {

        log.info("生成订单");
        //生成订单
        OrderInfo orderInfo=orderInfoService.createOrderByProductId(productId);
        String codeUrl=orderInfo.getCodeUrl();
        if(orderInfo!=null && !StringUtils.isEmpty(codeUrl)){

            log.info("订单已存在，二维码已保存");
            //返回二维码
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;

        }


        log.info("凋用统一下单API");
        // 凋用统一下单API

        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));
        // 请求body参数
        Gson gson = new Gson();
        Map paramsMap = new HashMap<>();
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", orderInfo.getTitle());
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        Map amountmap = new HashMap();
        amountmap.put("total", orderInfo.getTotalFee());
        amountmap.put("currency", "CNY");
        paramsMap.put("amount", amountmap);
        //将参数据转换成JSON
        String jsonParams = gson.toJson(paramsMap);

        log.info("请求参数：" + jsonParams);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {

            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //处理成功
                log.info("成功,返回结果 = " + bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("失败,响应码 = " + statusCode + ",返回结果  = " + bodyAsString);
                throw new IOException("request failed");
            }
            //响应结果
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            //解析出二维码
            codeUrl = resultMap.get("code_url");
            log.info("code_url is: "+codeUrl);
            //保存二维码
            String orderNo=orderInfo.getOrderNo();
            orderInfoService.saveCodeUrl(orderNo,codeUrl);


            //返回二维码
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        } finally {
            response.close();
        }
    }
}
