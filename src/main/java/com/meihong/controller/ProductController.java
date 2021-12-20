package com.meihong.controller;

import com.meihong.entity.Product;
import com.meihong.service.ProductService;
import com.meihong.vo.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@CrossOrigin
@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @ApiOperation("测试接口")
    @GetMapping("/test")
    public R hello(){
        return R.ok().data("message","hello")
                .data("now",new Date());
    }

    @GetMapping("/list")
    public R list(){
        List<Product> list = productService.list();
        //this one
        System.out.println("分支更新11");
        System.out.println("分支更新22");
        System.out.println("分支更新33");
        System.out.println("分支更新44");
        System.out.println("分支更新55");
        System.out.println("分支更新42");
        System.out.println("分支更新42");
        return R.ok().data("productList", list);
    }

}
