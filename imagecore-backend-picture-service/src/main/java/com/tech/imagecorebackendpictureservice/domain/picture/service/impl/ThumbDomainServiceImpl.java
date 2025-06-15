package com.tech.imagecorebackendpictureservice.domain.picture.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.utils.CacheUtils;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.RedisKeyUtil;
import com.tech.imagecorebackendmodel.picture.entity.Thumb;
import com.tech.imagecorebackendmodel.picture.valueobject.LuaStatusEnum;
import com.tech.imagecorebackendmodel.picture.valueobject.RedisLuaScriptConstant;
import com.tech.imagecorebackendmodel.picture.valueobject.ThumbConstant;
import com.tech.imagecorebackendmodel.picture.valueobject.ThumbTypeEnum;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.picture.DoThumbRequest;
import com.tech.imagecorebackendmodel.vo.picture.PictureVO;
import com.tech.imagecorebackendpictureservice.domain.picture.service.ThumbDomainService;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.CacheManager;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.ThumbMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Remon
* @description 针对表【thumb】的数据库操作Service实现
*/
@Service
public class ThumbDomainServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
    implements ThumbDomainService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager cacheManager;

    private String getTimeSlice() {
        DateTime nowDate = DateUtil.date();
        // 获取到当前时间前最近的整数秒，比如当前 11:20:23 ，获取到 11:20:20
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }

    public void putCaffineIfPresent(User loginUser, Long pictureId, Integer thumbState){
        String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
        String fieldKey = pictureId.toString();
        cacheManager.putIfPresent(hashKey, fieldKey, thumbState);
        cacheManager.putThumbCountIfPresent(CacheUtils.getPictureCacheKey(fieldKey), thumbState);
    }

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, User loginUser) {
        if (doThumbRequest == null || doThumbRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long pictureId = doThumbRequest.getPictureId();

        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey =  RedisKeyUtil.buildRedisKey(RedisKeyUtil.getTempThumbKey(timeSlice));
        String userThumbKey =  RedisKeyUtil.buildRedisKey(RedisKeyUtil.getUserThumbKey(loginUser.getId()));
        String pictureKey = RedisKeyUtil.buildRedisKey(CacheUtils.getPictureCacheKey(pictureId.toString()));

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey, pictureKey),
                loginUser.getId(),
                pictureId
        );

        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已点赞");
        }
        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, pictureId, ThumbTypeEnum.INCR.getValue());

        // 更新成功才执行
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, User loginUser) {
        if (doThumbRequest == null || doThumbRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long pictureId = doThumbRequest.getPictureId();
        // 计算时间片
        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey =  RedisKeyUtil.buildRedisKey(RedisKeyUtil.getTempThumbKey(timeSlice));
        String userThumbKey =  RedisKeyUtil.buildRedisKey(RedisKeyUtil.getUserThumbKey(loginUser.getId()));
        String pictureKey = RedisKeyUtil.buildRedisKey(CacheUtils.getPictureCacheKey(pictureId.toString()));

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey, pictureKey),
                loginUser.getId(),
                pictureId
        );
        // 根据返回值处理结果
        if (result == LuaStatusEnum.FAIL.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未点赞");
        }

        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, pictureId, ThumbTypeEnum.DECR.getValue());

        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean hasThumb(Long pictureId, Long userId) {
        Object thumbIdObj = cacheManager.getThumbCache(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, pictureId.toString());
        if (thumbIdObj == null) {
            return false;
        }
        Long thumbId = ((Number) thumbIdObj).longValue();
        return !thumbId.equals(ThumbConstant.UN_THUMB_CONSTANT);
    }

    @Override
    public List<PictureVO> getPictureThumbState(List<PictureVO> pictureVOList, User loginUser) {
        String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
        List<String> pictureIdList = pictureVOList.stream()
                .map(pictureVO -> pictureVO.getId().toString())
                .collect(Collectors.toList());
        Map<Long, Boolean> PictureThumbMap = cacheManager.getThumbMapCache(hashKey, pictureIdList);
        for(PictureVO pictureVO: pictureVOList){
            if (PictureThumbMap.containsKey(pictureVO.getId())){
                pictureVO.setHasThumb(Boolean.TRUE);
            }else {
                pictureVO.setHasThumb(Boolean.FALSE);
            }
        }
        return pictureVOList;
    }
}




