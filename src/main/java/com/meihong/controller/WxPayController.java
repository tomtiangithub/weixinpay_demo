package com.meihong.controller;

import com.google.gson.Gson;
import com.meihong.service.WxPayService;
import com.meihong.util.HttpUtils;
import com.meihong.util.WechatPay2ValidatorForRequest;
import com.meihong.vo.R;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
@RequestMapping("/api/wx-pay")
@Api(tags = "网站微信支付API")
@Slf4j
public class WxPayController {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private Verifier verifier;

    @ApiOperation("调用统一下单API,生成二维码")
    @PostMapping("native/{productId}")
    public R nativePay(@PathVariable Long productId) throws Exception {
        log.info("发起支付请求");
        //返回支付二维码链接和订单号
        Map<String, Object> map = wxPayService.nativePay(productId);
        //R r=R.ok();
        //r.setData(map);
        //return r;
        return R.ok().setData(map);
    }

    @ApiOperation("支付通知")
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();
        //处理通知参数
        String body = HttpUtils.readData(request);
        Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
        String requestId = (String) bodyMap.get("id");
        log.info("支付通知的ID ===>{}", bodyMap.get("id"));
        log.info("支付通知的完整数据 ===> {}", body);

        //TODO:签名的验证
        WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest
                = new WechatPay2ValidatorForRequest(verifier, requestId, body);
        if (!wechatPay2ValidatorForRequest.validate(request)) {
            log.info("通知验签失败");
            //失败应答
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "通知验签失败");
            return gson.toJson(map);
        }
        log.info("通知验签成功");
        //TODO:处理订单
        wxPayService.processOrder(bodyMap);
        //应答超时
        TimeUnit.SECONDS.sleep(5);
        //模拟接收微信端的重复通知
        //  int t=10/0;
        response.setStatus(200);
        map.put("code", "SUCCESS");
        return gson.toJson(map);
    }

    @ApiOperation("取消订单")
    @PostMapping("/cancel/{orderNo}")
    public R cancel(@PathVariable String orderNo) throws Exception {
       log.info("取消订单");
       wxPayService.cancelOrder(orderNo);
       return R.ok().setMessage("订单已取消");
    }

    @ApiOperation("查询订单")
    @GetMapping("/query/{orderNo}")
    public R queryOrder(@PathVariable String orderNo) throws Exception {
      log.info("查询订单");
      String result=wxPayService.queryOrder(orderNo);

      return R.ok().setMessage("查询成功").data("result",result);
    }
}
