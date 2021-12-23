package com.meihong.controller;

import com.meihong.entity.OrderInfo;
import com.meihong.service.OrderInfoService;
import com.meihong.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/order-info")
@Api(tags = "商品订单管理")
public class OrderInfoController {

    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public R list(){
       List<OrderInfo> list= orderInfoService.listOrderByCreateTimeDesc();
       return R.ok().data("list",list);
    }
}
