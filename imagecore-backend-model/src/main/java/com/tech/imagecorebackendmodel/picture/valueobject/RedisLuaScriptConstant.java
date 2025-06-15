package com.tech.imagecorebackendmodel.picture.valueobject;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @author pine
 */
public class RedisLuaScriptConstant {

    private static final String THUMB_SCRIPT_STR =
            "local tempThumbKey = KEYS[1]       -- 临时计数键（如 imagocore:thumb:temp:{timeSlice}）\n" +
            "local userThumbKey = KEYS[2]       -- 用户点赞状态键（如 imagocore:thumb:{userId}）\n" +
            "local pictureKey = KEYS[3]       -- 图片Id键（如 imagocore:pic:{pictureId}）\n" +
            "local userId = ARGV[1]             -- 用户 ID\n" +
            "local pictureId = ARGV[2]             -- 图片 ID\n" +
            "\n" +
            "-- 1. 检查是否已点赞（避免重复操作）\n" +
            "if redis.call('HEXISTS', userThumbKey, pictureId) == 1 then\n" +
            "   return -1  -- 已点赞，返回 -1 表示失败\n" +
            "end\n" +
            "\n" +
            "-- 2. 获取旧值（不存在则默认为 0）\n" +
            "local hashKey = userId .. ':' .. pictureId\n" +
            "local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)\n" +
            "local oldThumbCount = tonumber(redis.call('GET', pictureKey) or 0)\n" +
            "\n" +
            "-- 3. 计算新值\n" +
            "local newNumber = oldNumber + 1\n" +
            "local newThumbCount = oldThumbCount + 1\n" +
            "\n" +
            "-- 4. 原子性更新：写入临时计数 + 标记用户已点赞\n" +
            "redis.call('HSET', tempThumbKey, hashKey, newNumber)\n" +
            "redis.call('SET', pictureKey, newThumbCount)\n" +
            "redis.call('HSET', userThumbKey, pictureId, 1)\n" +
            "return 1  -- 返回 1 表示成功\n";

    /**
     * 点赞 Lua 脚本
     * KEYS[1]       -- 临时计数键
     * KEYS[2]       -- 用户点赞状态键
     * ARGV[1]       -- 用户 ID
     * ARGV[2]       -- 博客 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> THUMB_SCRIPT = new DefaultRedisScript<>(THUMB_SCRIPT_STR, Long.class);

    private static final String UNTHUMB_SCRIPT_STR =
            "local tempThumbKey = KEYS[1]      -- 临时计数键（如 imagocore:thumb:temp:{timeSlice}）\n" +
            "local userThumbKey = KEYS[2]      -- 用户点赞状态键（如 imagocore:thumb:{userId}）\n" +
            "local pictureKey = KEYS[3]       -- 图片Id键（如 imagocore:pic:{pictureId}）\n" +
            "local userId = ARGV[1]            -- 用户 ID\n" +
            "local pictureId = ARGV[2]            -- 图片 ID\n" +
            "-- 1. 检查用户是否已点赞（若未点赞，直接返回失败）\n" +
            "if redis.call('HEXISTS', userThumbKey, pictureId) ~= 1 then\n" +
            "   return -1  -- 未点赞，返回 -1 表示失败\n" +
            "end\n" +
            "\n" +
            "-- 2. 获取当前临时计数（若不存在则默认为 0）\n" +
            "local hashKey = userId .. ':' .. pictureId\n" +
            "local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)\n" +
            "local oldThumbCount = tonumber(redis.call('GET', pictureKey) or 0)\n" +
            "-- 3. 计算新值并更新\n" +
            "local newNumber = oldNumber - 1\n" +
            "local newThumbCount = oldThumbCount - 1\n" +
            "\n" +
            "-- 4. 原子性操作：更新临时计数 + 删除用户点赞标记\n" +
            "redis.call('HSET', tempThumbKey, hashKey, newNumber)\n" +
            "redis.call('SET', pictureKey, newThumbCount)\n" +
            "redis.call('HDEL', userThumbKey, pictureId)\n" +
            "\n" +
            "return 1  -- 返回 1 表示成功\n";

    /**
     * 取消点赞 Lua 脚本
     * 参数同上
     * 返回：
     * -1: 未点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> UNTHUMB_SCRIPT = new DefaultRedisScript<>(UNTHUMB_SCRIPT_STR, Long.class);

}
