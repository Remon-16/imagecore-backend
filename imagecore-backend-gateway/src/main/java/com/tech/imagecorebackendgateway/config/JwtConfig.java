package com.tech.imagecorebackendgateway.config;

import com.tech.imagecorebackendcommon.utils.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {
    @Bean
    public SecretKey secretKey() {
        return JwtUtils.createSecretKey();
    }
}
