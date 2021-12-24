package com.meihong.task;

import com.meihong.entity.OrderInfo;
import com.meihong.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class WxPayTask {

    @Resource
    private OrderInfoService orderInfoService;

    /**
     *秒分时日月周
     * *：
     * ？：不指定
     * 日，月，周不能同时指定 ，指定其中之一，则另一个指定为？
     */
  @Scheduled(cron = "0/3 * * * * ?")
  public void task1(){
    log.info("task1 被执行......");
  }

    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm(){
        log.info("orderConfirm 被执行......");

       List<OrderInfo> orderInfoList= orderInfoService.getNoPayOrderByDuration(5);
        for (OrderInfo orderInfo:orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单 ===> {}",orderNo);
            //核实订单状态，调用微信支付查单接口

        }
    }

}
