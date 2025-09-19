package com.tech.imagecorebackendpictureservice;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan("com.tech")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.tech.imagecorebackendserviceclient.application.service"})
@EnableAsync
@EnableScheduling
@MapperScan("com.tech.imagecorebackendpictureservice.infrastructure.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@Import(RocketMQAutoConfiguration.class)
public class ImagecoreBackendPictureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImagecoreBackendPictureServiceApplication.class, args);
    }

}
