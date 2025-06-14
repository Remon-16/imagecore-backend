package com.tech.imagecorebackendpictureservice.infrastructure.dco;


import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;

import com.tech.imagecorebackendpictureservice.infrastructure.algorithm.AddResult;
import com.tech.imagecorebackendpictureservice.infrastructure.algorithm.TopK;
import com.tech.imagecorebackendcommon.utils.CacheUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CacheManager {

    @Resource
    private TopK hotKeyDetector;

    @Resource
    private Cache<String, Object> localCache;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Getter
    @Setter
    private Integer redisExpireTime = 300;


    // 辅助方法：构造复合 key
    private String buildCacheKey(String hashKey, String key) {
        return hashKey + ":" + key;
    }

    public Object getValueCache(String key){
        // 1. 先查本地缓存
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            log.info("本地缓存获取到数据 {} = {}", key, value);
            // 记录访问次数（每次访问计数 +1）
            hotKeyDetector.add(key, 1);
            return value;
        }

        // 2. 本地缓存未命中，查询 Redis
        Object redisValue = redisTemplate.opsForValue().get(RedisKeyUtil.buildRedisKey(key));
        if (redisValue == null) {
            return null;
        }

        // 3. 记录访问（计数 +1）
        AddResult addResult = hotKeyDetector.add(key, 1);

        // 4. 如果是热 Key 且不在本地缓存，则缓存数据
        if (addResult.isHotKey()) {
            localCache.put(key, redisValue);
        }

        return redisValue;
    }

    public Map<Long, Boolean> getThumbMapCaffeine(String hashKey, List<String> pictureIdList){
        Map<Long, Boolean> pictureIdHasThumbMap = new HashMap<>();

        List<String> compositeKeyList = pictureIdList.stream()
                .map(id -> buildCacheKey(hashKey, id))
                .collect(Collectors.toList());
        for (int i = 0; i < pictureIdList.size(); i++) {
            String compositeKey = compositeKeyList.get(i);
            Object value = localCache.getIfPresent(compositeKey);
            if (value != null) {
                log.info("本地缓存获取到数据 {} = {}", compositeKey, value);
                // 记录访问次数（每次访问计数 +1）
                hotKeyDetector.add(pictureIdList.get(i), 1);
                Long thumbId = (Long) value;
                if(thumbId.equals(1L)){
                    pictureIdHasThumbMap.put(Long.parseLong(compositeKey), Boolean.TRUE);
                }
            }
        }
        return pictureIdHasThumbMap;
    }

    public Map<Long, Boolean> getThumbMapRedis(String hashKey, List<String> pictureIdList){
        Map<Long, Boolean> pictureIdHasThumbMap = new HashMap<>();
        List<Object> objectList = new ArrayList<>(pictureIdList);
        // 获取点赞
        List<Object> thumbList = redisTemplate.opsForHash().multiGet(
                RedisKeyUtil.buildRedisKey(hashKey), objectList);
        for (int i = 0; i < thumbList.size(); i++) {
            if (thumbList.get(i) == null) {
                continue;
            }
            pictureIdHasThumbMap.put(Long.valueOf(objectList.get(i).toString()), Boolean.TRUE);
        }
        return pictureIdHasThumbMap;
    }

    public Map<Long, Boolean> getThumbMapCache(String hashKey, List<String> pictureIdList){
        Map<Long, Boolean> pictureIdHasThumbMap = getThumbMapCaffeine(hashKey, pictureIdList);
        List<String> missKeys = new ArrayList<>();
        for (String pictureId : pictureIdList) {
            if (!pictureIdHasThumbMap.containsKey(Long.parseLong(pictureId))) {
                missKeys.add(pictureId);
            }
        }
        if (!missKeys.isEmpty()) {
            pictureIdHasThumbMap.putAll(getThumbMapRedis(hashKey, missKeys));
        }
        return pictureIdHasThumbMap;
    }

    public Map<Long, Long> getThumbCountCaffeine(List<String> pictureKeyList){
        Map<Long, Long> pictureIdThumbCountMap = new HashMap<>();
        for (String key : pictureKeyList) {
            Object value = localCache.getIfPresent(key);
            if (value != null) {
                log.info("本地缓存获取到数据 {} = {}", key, value);
                // 记录访问次数（每次访问计数 +1）
                hotKeyDetector.add(key, 1);
                pictureIdThumbCountMap.put(Long.parseLong(key), (Long) value);
            }
        }
        return pictureIdThumbCountMap;
    }

    private Long pictureKey2PictureId(String pictureKey){
        // 直接从索引4开始截取
        String numberStr = pictureKey.substring(4);
        return Long.parseLong(numberStr);
    }

    public Map<Long, Long> getThumbCountRedis(List<String> pictureKeyList){
        Map<Long, Long> pictureIdThumbCountMap = new HashMap<>();
        List<String> pictureRedisKeyList = pictureKeyList.stream()
                .map(RedisKeyUtil::buildRedisKey).collect(Collectors.toList());
        // 获取点赞
        List<Object> thumbList = redisTemplate.opsForValue().multiGet(pictureRedisKeyList);
        if(thumbList == null || thumbList.isEmpty()){
            return pictureIdThumbCountMap;
        }
        for (int i = 0; i < thumbList.size(); i++) {
            Object thumbCount = thumbList.get(i);
            if (thumbCount == null) {
                continue;
            }
            Long value = ((Number) thumbCount).longValue();
            pictureIdThumbCountMap.put(pictureKey2PictureId(pictureKeyList.get(i)), value);
        }
        return pictureIdThumbCountMap;
    }

    public Map<Long, Long> getThumbCountCache(List<Long> pictureIdList){
        List<String> pictureKeyList = pictureIdList.stream()
                .map(pictureId -> CacheUtils.getPictureCacheKey(pictureId.toString()))
                .collect(Collectors.toList());
        // 1. 查本地缓存
        Map<Long, Long> pictureIdThumbCountMap = getThumbCountCaffeine(pictureKeyList);
        List<String> missKeys = new ArrayList<>();
        // 2. 收集 Miss Key
        for (String key : pictureKeyList) {
            if (!pictureIdThumbCountMap.containsKey(pictureKey2PictureId(key))) {
                missKeys.add(key);
            }
        }
        // 3. 查 Redis
        pictureIdThumbCountMap.putAll(getThumbCountRedis(missKeys));
        return pictureIdThumbCountMap;
    }

    public Object getThumbCache(String hashKey, String key) {
        // 构造唯一的 composite key
        String compositeKey = buildCacheKey(hashKey, key);

        // 1. 先查本地缓存
        Object value = localCache.getIfPresent(compositeKey);
        if (value != null) {
            log.info("本地缓存获取到数据 {} = {}", compositeKey, value);
            // 记录访问次数（每次访问计数 +1）
            hotKeyDetector.add(key, 1);
            return value;
        }

        // 2. 本地缓存未命中，查询 Redis
        Object redisValue = redisTemplate.opsForHash().get(RedisKeyUtil.buildRedisKey(hashKey), key);
        if (redisValue == null) {
            return null;
        }

        // 3. 记录访问（计数 +1）
        AddResult addResult = hotKeyDetector.add(key, 1);

        // 4. 如果是热 Key 且不在本地缓存，则缓存数据
        if (addResult.isHotKey()) {
            localCache.put(compositeKey, redisValue);
        }

        return redisValue;
    }

    public void putValueToCache(String key,Object value){
        // 1. 记录访问（计数 +1）
        AddResult addResult = hotKeyDetector.add(key, 1);
        if (addResult.isHotKey()) {
            // 2. 存本地缓存
            localCache.put(key, value);
        }
        // 3. 存 Redis
        int redisCacheExpireTime = redisExpireTime +  RandomUtil.randomInt(0, redisExpireTime);
        redisTemplate.opsForValue().set(RedisKeyUtil.buildRedisKey(key),
                value, redisCacheExpireTime, TimeUnit.SECONDS);
    }

    public void putIfPresent(String hashKey, String key, Integer value) {
        String compositeKey = buildCacheKey(hashKey, key);
        Object object = localCache.getIfPresent(compositeKey);
        if (object == null) {
            return;
        }
        Integer oldValue = (Integer) object;
        localCache.put(compositeKey, oldValue + value);
    }

    public void putThumbCountIfPresent(String key, Integer count) {
        Object object = localCache.getIfPresent(key);
        if (object == null) {
            return;
        }
        Integer oldValue = (Integer) object;
        localCache.put(key, oldValue + count);
    }

    // 定时清理过期的热 Key 检测数据
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    public void cleanHotKeys() {
        hotKeyDetector.fading();
    }

}
