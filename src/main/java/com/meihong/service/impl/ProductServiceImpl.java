package com.meihong.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meihong.entity.Product;
import com.meihong.mapper.ProductMapper;
import com.meihong.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}
