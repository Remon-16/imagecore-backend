package com.tech.imagecorebackendpictureservice.infrastructure.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tech.imagecorebackendcommon.utils.CacheUtils;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.user.constant.MessageConstant;
import com.tech.imagecorebackendmodel.user.constant.UserScoreConstant;
import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackendmodel.user.entity.ScoreUser;
import com.tech.imagecorebackendmodel.user.valueobject.MessageType;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.RedisKeyUtil;
import com.tech.imagecorebackendmodel.picture.entity.Thumb;
import com.tech.imagecorebackendmodel.picture.valueobject.ThumbTypeEnum;
import com.tech.imagecorebackendpictureservice.domain.picture.service.ThumbDomainService;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.PictureMapper;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Resource
    private UserFeignClient userFeignClient;

    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("开始执行");
        DateTime nowDate = DateUtil.date();
        String date = DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10 - 1) * 10;
        syncThumb2DBByDate(date);
        syncScore2DBByDate(date);
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
        // 批量更新图片点赞量，增加被点赞的用户积分
        if (!pictureThumbCountMap.isEmpty()) {
            pictureMapper.batchUpdateThumbCount(pictureThumbCountMap);
            List<Long> pictureIdList = thumbList.stream().map(Thumb::getPictureId).toList();
            List<Picture> pictureList = pictureMapper.selectByIds(pictureIdList);
            List<ScoreUser> scoreUserList = new ArrayList<>();
            Map<Long, Long> userScoreChangeMap = new HashMap<>();
            List<Message> messageList = new ArrayList<>();
            for (int i=0; i < pictureList.size(); i++) {
                Picture picture = pictureList.get(i);
                Thumb thumb = thumbList.get(i);
                Long curScore = UserScoreConstant.BETHUMBNAIL_PICTURE_VALUE;
                ScoreUser scoreUser = new ScoreUser();
                scoreUser.setUserId(picture.getUserId());
                scoreUser.setScoreAmount(curScore);
                scoreUser.setScoreType(UserScoreConstant.BETHUMBNAIL_PICTURE);
                scoreUserList.add(scoreUser);

                Long userId = picture.getUserId();
                Long oldScore = userScoreChangeMap.getOrDefault(userId, 0L);
                userScoreChangeMap.put(userId, oldScore + curScore);

                Message message = new Message();
                message.setUserId(userId);
                message.setPictureId(thumb.getPictureId());
                message.setCommentId(thumb.getId());
                message.setMessageType(MessageType.THUMB.getValue());
                message.setContent(MessageConstant.NEW_THUMB);
                message.setSenderId(MessageConstant.SYSTEM_SENDER_ID);
                message.setMessageState("0");
                messageList.add(message);
            }
            userFeignClient.saveBatch(scoreUserList);
            // 批量发送消息给用户
            userFeignClient.messageBatchSend(messageList);
            // 批量更新用户积分余额
            if (!userScoreChangeMap.isEmpty()) {
                userFeignClient.batchUpdateScore(userScoreChangeMap);
            }
        }

        // 使用虚拟线程异步删除
        Thread.startVirtualThread(() -> {
            redisTemplate.delete(tempThumbKey);
        });
    }

    public void syncScore2DBByDate(String date){
        String tempScoreKey = RedisKeyUtil.buildRedisKey(CacheUtils.getUserTempScoreCacheKey(date));
        Map<Object, Object> allScoreThumbMap = redisTemplate.opsForHash().entries(tempScoreKey);
        boolean scoreMapEmpty = CollUtil.isEmpty(allScoreThumbMap);

        if (scoreMapEmpty) {
            return;
        }

        List<ScoreUser> scoreUserList = new ArrayList<>();
        Map<Long, Long> userScoreChangeMap = new HashMap<>();
        for (Object userIdScoreObj : allScoreThumbMap.keySet()) {
            String userIdScoreType = (String) userIdScoreObj;
            String[] userIdAndScoreType = userIdScoreType.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndScoreType[0].replace("\"", ""));
            Long oldScore = userScoreChangeMap.getOrDefault(userId, 0L);
            Long curScore = Long.parseLong(String.valueOf(allScoreThumbMap.get(userIdScoreType)));
            userScoreChangeMap.put(userId, oldScore + curScore);
            String scoreType = userIdAndScoreType[1].replace("\"", "");
            ScoreUser scoreUser = new ScoreUser();
            scoreUser.setUserId(userId);
            scoreUser.setScoreType(scoreType);
            scoreUser.setScoreAmount(curScore);
            scoreUserList.add(scoreUser);
        }
        userFeignClient.saveBatch(scoreUserList);
        // 批量更新用户积分余额
        if (!userScoreChangeMap.isEmpty()) {
            userFeignClient.batchUpdateScore(userScoreChangeMap);
        }
        redisTemplate.delete(tempScoreKey);
    }

}
