package com.tech.imagecorebackendpictureservice.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "imagocore.caffeine")
@Data
public class CaffeineConfig {
    /**
     * 初始容量
     */
    @Value("imagocore.caffeine.initial-capacity")
    private String initialCapacity;
    /**
     * 最大容量
     */
    @Value("imagocore.caffeine.maximum-size")
    private String maximumSize;
    /**
     * 过期时间
     */
    @Value("imagocore.caffeine.expire-time")
    private String expireTime;

    /**
     * 存储超热门数据
     * @return
     */
    @Bean
    public Cache<String, Object> localCache(){
        return Caffeine.newBuilder().initialCapacity(Integer.parseInt(initialCapacity))
                .maximumSize(Long.parseLong(maximumSize))
                // 缓存 5 分钟移除
                .expireAfterWrite(Long.parseLong(expireTime), TimeUnit.MINUTES)
                .build();
    }

}
