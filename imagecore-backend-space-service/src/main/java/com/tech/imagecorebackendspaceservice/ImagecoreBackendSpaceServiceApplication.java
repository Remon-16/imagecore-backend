package com.tech.imagecorebackendspaceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan("com.tech")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.tech.imagecorebackendserviceclient.application.service"})
@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
public class ImagecoreBackendSpaceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImagecoreBackendSpaceServiceApplication.class, args);
    }

}
