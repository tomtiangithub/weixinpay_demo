package com.meihong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.meihong.config.WxPayConfig;
import com.meihong.entity.OrderInfo;
import com.meihong.enums.OrderStatus;
import com.meihong.enums.wxpay.WxApiType;
import com.meihong.enums.wxpay.WxNotifyType;
import com.meihong.service.OrderInfoService;
import com.meihong.service.PaymentInfoService;
import com.meihong.service.WxPayService;
import com.meihong.util.OrderNoUtils;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Resource
    private WxPayConfig wxPayConfig;
    @Resource
    private CloseableHttpClient wxPayClient;
    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private PaymentInfoService paymentInfoService;
    private final ReentrantLock lock=new ReentrantLock();

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

    @Override
    public void processOrder(Map<String, Object> bodyMap) throws Exception {
        log.info("处理订单");
        //解密报文
        String plainText=decrypFromResourc(bodyMap);
        //将明文转换成map
        Gson gson = new Gson();
        HashMap plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String) plainTextMap.get("out_trade_no");

             /* 在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，
        以避免函数重入造成的数据混乱。*/
            //尝试获取锁，成功获取立即返回true,获取失几返回false,不必一直等等锁的释放。
            if(lock.tryLock()){

                try {
                //处理重复的通知
                //接口调用的幂等性，无论接口调用多少次，产生的结果是一致的。
                String orderStatus=orderInfoService.getOrderStatus(orderNo);
                if(!OrderStatus.NOTPAY.getType().equals(orderStatus)){
                    return;
                  }
                  /*  //处理重复的通知
                    //接口调用的幂等性，无论接口调用多少次，产生的结果是一致的。
                    String orderStatus=orderInfoService.getOrderStatus(orderNo);
                    if(!OrderStatus.NOTPAY.getType().equals(orderStatus)){
                        return;
                    }*/
                    //模拟通知并发
                    TimeUnit.SECONDS.sleep(5);

                    //更新订单状态
                    orderInfoService.updateStatusByOrderNo(orderNo,OrderStatus.SUCCESS);
                    //记录支付日志
                    paymentInfoService.createPaymentInfo(plainText);

                }finally {
                    //主动释放锁
                    lock.unlock();
                }
            }
    }

    /**
     * 取消订单
     * @param orderNo
     */
    @Override
    public void cancelOrder(String orderNo) throws Exception {

        //调用微信支付的关单接口
        this.closeOrder(orderNo);
        //更新商户端的订单状态
        orderInfoService.updateStatusByOrderNo(orderNo,OrderStatus.CANCEL);
    }

    /**
     * 查询订单
     * @param orderNo
     * @return
     */
    @Override
    public String queryOrder(String orderNo) throws Exception {
      log.info("查单接口的调用 ===> {}",orderNo);

      String url=String.format(WxApiType.ORDER_QUERY_BY_NO.getType(),orderNo);
      url=wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());

      HttpGet httpGet = new HttpGet(url);
      httpGet.setHeader("Accept", "application/json");
      //完成签名并执行请求
      CloseableHttpResponse response = wxPayClient.execute(httpGet);

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

            return bodyAsString;
        } finally {
            response.close();
        }
    }

    /**
     * 关闭订单接口的调用
     * @param orderNo
     */
    private void closeOrder(String orderNo) throws Exception {
      log.info("关闭订单接口 ===> {}",orderNo);
      //创建远程请求对象
      String url=String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(),orderNo);
      url=wxPayConfig.getDomain().concat(url);
      HttpPost httpPost = new HttpPost(url);
      //组装json请求体
      Gson gson = new Gson();
      Map<String, String> paramsMap = new HashMap<>();
      paramsMap.put("mchid",wxPayConfig.getMchId());
      //paramsMap.put("out_trade_no",out_trade_no);
      String jsonParams = gson.toJson(paramsMap);
      log.info("请求参数 ===> {}",jsonParams);
      //将请求参数设置到请求对象中
      StringEntity entity = new StringEntity(jsonParams, "utf-8");
      entity.setContentType("application/json");
      httpPost.setEntity(entity);
      httpPost.setHeader("Accept", "application/json");
      //完成签名并执行请求
      CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //处理成功
                log.info("成功200");
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功204");
            } else {
                log.info("失败,响应码 = " + statusCode);
                throw new IOException("request failed");
            }

        } finally {
            response.close();
        }

    }

    /**
     * 对称解密
     * @param bodyMap
     * @return
     */
    private String decrypFromResourc(Map<String, Object> bodyMap) throws Exception {
        log.info("密文解密");
        AesUtil aesUtil = new AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));

        //通知数据
        Map<String,String> resourceMap = (Map) bodyMap.get("resource");
        //数据密文
        String ciphertext=resourceMap.get("ciphertext");

        log.info("密文 ===> {}",ciphertext);
        //随机串
        String nonce = resourceMap.get("nonce");
        //附加数据
        String associated_data = resourceMap.get("associated_data");

        String plainText=aesUtil.decryptToString(associated_data.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext);
        log.info("明文 ===> {}",plainText);

        return plainText;
    }
}
