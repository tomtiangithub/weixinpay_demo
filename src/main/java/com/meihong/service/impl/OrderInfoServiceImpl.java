package com.meihong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meihong.entity.OrderInfo;
import com.meihong.entity.Product;
import com.meihong.enums.OrderStatus;
import com.meihong.mapper.OrderInfoMapper;
import com.meihong.mapper.ProductMapper;
import com.meihong.service.OrderInfoService;
import com.meihong.util.OrderNoUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Resource
    private ProductMapper productMapper;

    @Override
    public OrderInfo createOrderByProductId(Long productId) {

        //查找已存在未支付的订单
        OrderInfo orderInfo = this.getNoPayOrderByProductId(productId);
        if(orderInfo!=null){
           return orderInfo;
        }

        //获取商品信息
        Product product=productMapper.selectById(productId);
        // 生成订单
        orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle());
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(product.getPrice());
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        //TO_DO 订单存入数据库
        //orderInfoMapper.insert(orderInfo);
        baseMapper.insert(orderInfo);
        return orderInfo;
    }

    /**
     * 存取订单二维码
     * @param orderNo
     * @param codeUrl
     */
    @Override
    public void saveCodeUrl(String orderNo, String codeUrl) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderNo);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCodeUrl(codeUrl);
        baseMapper.update(orderInfo,queryWrapper);
    }

    @Override
    public List<OrderInfo> listOrderByCreateTimeDesc() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<OrderInfo>().orderByDesc("create_time");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据商品ID 查询未支付订单
     * @param productId
     * @return
     */
    private OrderInfo getNoPayOrderByProductId(Long productId) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id",productId);
        queryWrapper.eq("order_status",OrderStatus.NOTPAY.getType());
//        queryWrapper.eq("user_id",userId);
        OrderInfo orderInfo=baseMapper.selectOne(queryWrapper);
        return orderInfo;
    }
}
