package com.meihong.service.impl;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meihong.entity.OrderInfo;
import com.meihong.entity.Product;
import com.meihong.enums.OrderStatus;
import com.meihong.mapper.OrderInfoMapper;
import com.meihong.mapper.ProductMapper;
import com.meihong.service.ProductService;
import com.meihong.util.OrderNoUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {




}
