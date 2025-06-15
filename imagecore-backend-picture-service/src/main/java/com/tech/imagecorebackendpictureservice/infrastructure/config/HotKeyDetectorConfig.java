package com.tech.imagecorebackendpictureservice.infrastructure.config;


import com.tech.imagecorebackendpictureservice.infrastructure.algorithm.HeavyKeeper;
import com.tech.imagecorebackendpictureservice.infrastructure.algorithm.TopK;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HotKeyDetectorConfig {

    @Bean
    public TopK hotKeyDetector() {
        return new HeavyKeeper(
                // 监控 Top 100 Key
                100,
                // 哈希表宽度
                100000,
                // 哈希表深度
                5,
                // 衰减系数
                0.92,
                // 最小出现 10 次才记录
                10
        );
    }
}
