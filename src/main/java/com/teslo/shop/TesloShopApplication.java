package com.teslo.shop;

import com.teslo.shop.config.AppProperties;
import com.teslo.shop.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ AppProperties.class, JwtProperties.class })
public class TesloShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(TesloShopApplication.class, args);
    }
}
