package com.tech.imagecorebackenduserservice;

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
public class ImagecoreBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImagecoreBackendUserServiceApplication.class, args);
    }

}
