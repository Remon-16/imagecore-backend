package com.tech.imagecorebackenduserservice.infrastructure.dco;

import cn.hutool.core.util.RandomUtil;
import com.tech.imagecorebackendcommon.utils.CacheUtils;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserCacheHandler {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 过期时间 秒
     */
    @Getter
    @Setter
    private Integer redisExpireTime = 86400;

    public static String getRedisKey(String key){
        return CacheUtils.APP_NAME + ":" + key;
    }

    public Long getUserCore(String key){
        Object redisValue = redisTemplate.opsForValue().get(getRedisKey(key));
        return redisValue == null ? null : Long.parseLong(redisValue.toString());
    }

    public void setUserCore(String key, Long value){
        int redisCacheExpireTime = redisExpireTime +  RandomUtil.randomInt(0, redisExpireTime);
        redisTemplate.opsForValue().set(getRedisKey(key),
                value, redisCacheExpireTime, TimeUnit.SECONDS);
    }

    public Long getUserAddScoreCount(String key){
        Object redisValue = redisTemplate.opsForValue().get(getRedisKey(key));
        return redisValue == null ? 0L : Long.parseLong(redisValue.toString());
    }
}
