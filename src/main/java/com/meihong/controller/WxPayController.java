package com.meihong.controller;

import com.google.gson.Gson;
import com.meihong.service.WxPayService;
import com.meihong.util.HttpUtils;
import com.meihong.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @ApiOperation("调用统一下单API,生成二维码")
    @PostMapping("native/{productId}")
    public R nativePay(@PathVariable Long productId) throws Exception {

        log.info("发起支付请求");
        //返回支付二维码链接和订单号
        Map<String,Object> map=wxPayService.nativePay(productId);
        //R r=R.ok();
        //r.setData(map);
        //return r;
        return R.ok().setData(map);
    }

    @ApiOperation("支付通知")
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) {
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();

            //处理通知参数
            String body=HttpUtils.readData(request);
            Map<String,Object> bodyMap = gson.fromJson(body, HashMap.class);
            log.info("支付通知的ID ===>{}",bodyMap.get("id"));
            log.info("支付通知的完整数据 ===> {}",body);

            //TODO:签名的验证
            //TODO:处理订单

      //  int t=10/0;

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        response.setStatus(200);
            map.put("code","SUCCESS");
            return gson.toJson(map);


    }
}
