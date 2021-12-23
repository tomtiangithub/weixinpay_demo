package com.meihong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//引入Spring Task
//@EnableScheduling
public class WeixinpayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeixinpayDemoApplication.class, args);
    }

}
