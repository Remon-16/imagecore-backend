package com.tech.imagecorebackendmodel.user.valueobject;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class UserRedisLuaScriptConstant {

    private static final String SCORE_HANDLE_SCRIPT_STR = """
            
            local tempScoreKey = KEYS[1]       -- 用户临时积分记录（如 imagocore:user:score:temp:{userId}:{timeSlice}）
            local userScoreKey = KEYS[2]       -- 用户积分余额（如 imagocore:user:score:{userId}）
            local userScoreCountKey = KEYS[3]  -- 用户积分获取次数记录（如 imagocore:user:scoreCount:{scoreType}:{userId}）
            local userId = ARGV[1]             -- 用户 ID
            local score = ARGV[2]             --  积分变化值
            
            -- 2. 获取当前积分值（若不存在则默认为 0）
            local oldScore = tonumber(redis.call('GET', userScoreKey) or 0)
            
            -- 3. 计算新积分
            local newScore = tonumber(score) + oldScore
            
            redis.call('SET', tempScoreKey, score)
            redis.call('SET', userScoreKey, timeSlice)
            
            -- 4. 如果是增加积分，需要更新限制次数
            if score > 0 then
                local oldCount = tonumber(redis.call('get', userScoreCountKey) or 0)
                local newCount = oldCount + 1
                redis.call('SET', userScoreCountKey, newCount)
            end
            
            return 1  -- 返回 1 表示成功
            """;
    /**
     * 积分变更 Lua 脚本
     * KEYS[1] 用户临时积分记录（如 imagocore:user:score:temp:{userId}:{timeSlice}）
     * KEYS[2] 用户积分余额（如 imagocore:user:score:{userId}）
     * KEYS[3] 用户积分获取次数记录（如 imagocore:user:score:count:{userId}）
     * ARGV[1] 用户 ID
     * ARGV[2] 积分变化值
     * 1: 操作成功
     */
    public static final RedisScript<Long> SCORE_HANDLE_SCRIPT = new DefaultRedisScript<>(SCORE_HANDLE_SCRIPT_STR, Long.class);
}
