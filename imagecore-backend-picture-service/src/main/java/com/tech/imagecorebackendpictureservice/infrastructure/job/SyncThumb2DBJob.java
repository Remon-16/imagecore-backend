package com.tech.imagecorebackendpictureservice.infrastructure.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.RedisKeyUtil;
import com.tech.imagecorebackendmodel.picture.entity.Thumb;
import com.tech.imagecorebackendmodel.picture.valueobject.ThumbTypeEnum;
import com.tech.imagecorebackendpictureservice.domain.picture.service.ThumbDomainService;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.PictureMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SyncThumb2DBJob {

    @Resource
    private ThumbDomainService thumbDomainService;

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("开始执行");
        DateTime nowDate = DateUtil.date();
        String date = DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10 - 1) * 10;
        syncThumb2DBByDate(date);
        log.info("临时数据同步完成");
    }

    public void syncThumb2DBByDate(String date) {
        // 获取到临时点赞和取消点赞数据
        // todo 如果数据量过大，可以分批读取数据
        String tempThumbKey = RedisKeyUtil.buildRedisKey(RedisKeyUtil.getTempThumbKey(date));
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        boolean thumbMapEmpty = CollUtil.isEmpty(allTempThumbMap);

        // 同步 点赞 到数据库
        // 构建插入列表并收集pictureId
        Map<Long, Long> pictureThumbCountMap = new HashMap<>();
        if (thumbMapEmpty) {
            return;
        }
        ArrayList<Thumb> thumbList = new ArrayList<>();
        LambdaQueryWrapper<Thumb> wrapper = new LambdaQueryWrapper<>();
        boolean needRemove = false;
        for (Object userIdPictureIdObj : allTempThumbMap.keySet()) {
            String userIdPictureId = (String) userIdPictureIdObj;
            String[] userIdAndPictureId = userIdPictureId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndPictureId[0]);
            Long pictureId = Long.valueOf(userIdAndPictureId[1]);
            // -1 取消点赞，1 点赞
            Integer thumbType = Integer.valueOf(allTempThumbMap.get(userIdPictureId).toString());
            if (thumbType == ThumbTypeEnum.INCR.getValue()) {
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setPictureId(pictureId);
                thumbList.add(thumb);
            } else if (thumbType == ThumbTypeEnum.DECR.getValue()) {
                // 拼接查询条件，批量删除
                // todo 数据量过大，可以分批操作
                needRemove = true;
                wrapper.or().eq(Thumb::getUserId, userId).eq(Thumb::getPictureId, pictureId);
            } else {
                if (thumbType != ThumbTypeEnum.NON.getValue()) {
                    log.warn("数据异常：{}", userId + "," + pictureId + "," + thumbType);
                }
                continue;
            }
            // 计算点赞增量
            pictureThumbCountMap.put(pictureId, pictureThumbCountMap.getOrDefault(pictureId, 0L) + thumbType);

        }
        // 批量插入
        thumbDomainService.saveBatch(thumbList);
        // 批量删除
        if (needRemove) {
            thumbDomainService.remove(wrapper);
        }
        // 批量更新图片点赞量
        if (!pictureThumbCountMap.isEmpty()) {
            pictureMapper.batchUpdateThumbCount(pictureThumbCountMap);
        }
        redisTemplate.delete(tempThumbKey);
    }

}
