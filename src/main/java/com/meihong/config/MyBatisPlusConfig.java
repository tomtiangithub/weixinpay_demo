package com.meihong.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan("com.meihong.mapper")
@EnableTransactionManagement
public class MyBatisPlusConfig {
}
