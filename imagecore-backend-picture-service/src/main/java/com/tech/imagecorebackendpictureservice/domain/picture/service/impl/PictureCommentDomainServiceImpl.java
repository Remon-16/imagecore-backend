package com.tech.imagecorebackendpictureservice.domain.picture.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendcommon.utils.CacheUtils;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentQueryRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentRequest;
import com.tech.imagecorebackendmodel.dto.user.UserChangeScoreRequest;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendmodel.user.constant.MessageConstant;
import com.tech.imagecorebackendmodel.user.constant.UserScoreConstant;
import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.user.valueobject.MessageType;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentRootVo;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;
import com.tech.imagecorebackendmodel.vo.user.UserListVO;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import com.tech.imagecorebackendpictureservice.domain.picture.service.PictureCommentDomainService;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.CacheManager;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.RedisKeyUtil;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.bean.SortedCacheResult;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.PictureCommentMapper;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.PictureMapper;
import com.tech.imagecorebackendpictureservice.infrastructure.mq.consumer.CanalHandleVo;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PictureCommentDomainServiceImpl extends ServiceImpl<PictureCommentMapper, PictureComment>
        implements PictureCommentDomainService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Getter
    @Setter
    private Integer expireTime = 1800;

    private final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public PictureComment saveOrUpdatePictureComment(PictureCommentRequest pictureCommentRequest) {
        ThrowUtils.throwIf(pictureCommentRequest == null,
                ErrorCode.PARAMS_ERROR, "pictureCommentRequest为空");
        PictureComment pictureComment = new PictureComment();
        BeanUtils.copyProperties(pictureCommentRequest, pictureComment);
        if (pictureComment.getId() == null) {
            UserChangeScoreRequest userChangeScoreRequest = new UserChangeScoreRequest();
            userChangeScoreRequest.setUserId(pictureComment.getUserId());
            userChangeScoreRequest.setScoreType(UserScoreConstant.PICTURE_COMMENT);
            this.save(pictureComment);
            // 增加积分
            userFeignClient.userAddScore(userChangeScoreRequest);
            // 给被评论的用户发消息
            this.sendMessage(pictureComment);
        }else {
            this.updateById(pictureComment);
        }
        return pictureComment;
    }

    private void sendMessage(PictureComment pictureComment) {
        Message message = new Message();

        if(pictureComment.getTargetId() == null) {
            // 一级评论
            Picture picture = pictureMapper.selectById(pictureComment.getPictureId());
            message.setUserId(picture.getUserId());
        }else if(pictureComment.getSecondTargetId() == null) {
            // 二级评论
            PictureComment targetPictureComment = this.getById(pictureComment.getTargetId());
            message.setUserId(targetPictureComment.getUserId());
        } else {
            // 三级评论
            PictureComment secondTargetPictureComment = this.getById(pictureComment.getSecondTargetId());
            message.setUserId(secondTargetPictureComment.getUserId());
        }
        message.setPictureId(pictureComment.getPictureId());
        message.setCommentId(pictureComment.getId());
        message.setMessageType(MessageType.COMMENT.getValue());
        message.setContent(MessageConstant.NEW_REPLY);
        message.setSenderId(MessageConstant.SYSTEM_SENDER_ID);
        message.setMessageState("0");
        userFeignClient.messageSend(message);
    }


    @Override
    public Boolean deletePictureComment(PictureCommentRequest pictureCommentRequest) {
        return this.removeById(pictureCommentRequest.getId());
    }

    @Override
    public QueryWrapper<PictureComment> getQueryWrapper(PictureCommentQueryRequest pictureCommentQueryRequest) {
        QueryWrapper<PictureComment> queryWrapper = new QueryWrapper<>();
        if (pictureCommentQueryRequest == null) {
            return queryWrapper;
        }
        Long pictureId = pictureCommentQueryRequest.getPictureId();
        Long targetId = pictureCommentQueryRequest.getTargetId();
        String sortField = pictureCommentQueryRequest.getSortField();
        String sortOrder = pictureCommentQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(pictureId), "pictureId", pictureId);
        if(ObjUtil.isNotEmpty(targetId)){
            queryWrapper.eq("targetId", targetId);
        }else{
            queryWrapper.isNull("targetId");
        }
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    private void getCommentUserInfo(PictureCommentVo pictureCommentVo, Map<Long, UserVO> commentUserMap, Long targetId) {
        // 先查这个集合里有没有，如果没有再去数据库查
        if(commentUserMap.containsKey(targetId)){
            UserVO user = commentUserMap.get(targetId);
            pictureCommentVo.setTargetUserId(user.getId());
            pictureCommentVo.setTargetUserName(user.getUserName());
        }else {
            PictureComment pictureComment = this.getById(targetId);
            User targetUser = userFeignClient.getUserById(pictureComment.getUserId());
            pictureCommentVo.setTargetUserId(targetUser.getId());
            pictureCommentVo.setTargetUserName(targetUser.getUserName());
            commentUserMap.put(targetId, userFeignClient.getUserVO(targetUser));
        }
    }

    private List<PictureCommentVo> getPictureCommentVo(List<PictureComment> pictureCommentList){
        if(pictureCommentList == null || pictureCommentList.isEmpty()){
            return new ArrayList<>();
        }
        Set<Long> userIdSet = pictureCommentList.stream().map(PictureComment::getUserId).collect(Collectors.toSet());
        UserListVO userListVO = userFeignClient.listByIds(userIdSet);
        List<User> userList = userListVO.getUserList(userListVO.getUserListJson());
        Map<Long, List<User>> userIdUserListMap = userList.stream()
                .collect(Collectors.groupingBy(User::getId));
        List<PictureCommentVo> pictureCommentVoList = pictureCommentList.stream().map(PictureCommentVo::objToVo).toList();

        Map<Long, UserVO> commentUserMap = new HashMap<>();
        pictureCommentVoList.forEach(pictureCommentVo -> {
            Long userId = pictureCommentVo.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureCommentVo.setUser(userFeignClient.getUserVO(user));
            commentUserMap.put(pictureCommentVo.getId(), pictureCommentVo.getUser());
        });

        // set target的用户相关信息
        pictureCommentVoList.forEach(pictureCommentVo -> {
            Long targetId = pictureCommentVo.getTargetId();
            Long secondTargetId = pictureCommentVo.getSecondTargetId();
            if(targetId != null && secondTargetId == null){
                this.getCommentUserInfo(pictureCommentVo, commentUserMap, targetId);
            }
            if (targetId != null && secondTargetId != null) {
                this.getCommentUserInfo(pictureCommentVo, commentUserMap, secondTargetId);
            }
        });

        return pictureCommentVoList;
    }

    @Override
    public Page<PictureCommentRootVo> getPictureCommentRootVo(PictureCommentQueryRequest pictureCommentQueryRequest) {
        ThrowUtils.throwIf(pictureCommentQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "pictureCommentQueryRequest为空");
        ThrowUtils.throwIf(pictureCommentQueryRequest.getPictureId() == null,
                ErrorCode.PARAMS_ERROR, "pictureId为空");
        long current = pictureCommentQueryRequest.getCurrent();
        long size = pictureCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        // 先查直接评论在图片上的 targetId == null
        pictureCommentQueryRequest.setTargetId(null);
        Page<PictureCommentVo> pictureCommentVoPage = queryPictureCommentVo(pictureCommentQueryRequest, current, size);
        // 接着通过评论的id，去查前10个子评论
        List<PictureCommentRootVo> pictureCommentRootVoList = new ArrayList<>();
        for(PictureCommentVo pictureCommentVo : pictureCommentVoPage.getRecords()){
            PictureCommentQueryRequest pictureCommentQueryRequest1 = new PictureCommentQueryRequest();
            pictureCommentQueryRequest1.setCurrent(1);
            pictureCommentQueryRequest1.setPageSize(10);
            pictureCommentQueryRequest1.setTargetId(pictureCommentVo.getId());
            pictureCommentQueryRequest1.setPictureId(pictureCommentVo.getPictureId());
            pictureCommentQueryRequest1.setSortOrder(pictureCommentQueryRequest.getSortOrder());
            Page<PictureCommentVo> pictureCommentVoPage2 = null;

            pictureCommentVoPage2 = this.getPictureCommentVo(pictureCommentQueryRequest1);
            PictureCommentRootVo pictureCommentRootVo = new PictureCommentRootVo();
            BeanUtils.copyProperties(pictureCommentVo, pictureCommentRootVo);
            pictureCommentRootVo.setPictureCommentVos(pictureCommentVoPage2);
            pictureCommentRootVoList.add(pictureCommentRootVo);
        }

        Page<PictureCommentRootVo> pictureCommentRootVoPage = new Page<>();
        pictureCommentRootVoPage.setRecords(pictureCommentRootVoList);
        pictureCommentRootVoPage.setCurrent(pictureCommentVoPage.getCurrent());
        pictureCommentRootVoPage.setTotal(pictureCommentVoPage.getTotal());
        pictureCommentRootVoPage.setSize(pictureCommentVoPage.getSize());
        return pictureCommentRootVoPage;
    }

    private Page<PictureCommentVo> queryCache(String sortedKey, String sortedTotalKey, String keyHead, String order, Long page, Long size){
        SortedCacheResult sortedCacheResult = cacheManager.querySortedValues(sortedKey, sortedTotalKey, keyHead, order, page, size);
        if(sortedCacheResult == null){
            return null;
        }
        Map<Object, Object> queryValueMap = sortedCacheResult.getValueMap();
        List<PictureCommentVo> pictureCommentVoList = new ArrayList<>();
        for(Object value : queryValueMap.values()){
            PictureCommentVo pictureCommentVo = JSONUtil.toBean((String)value, PictureCommentVo.class);
            pictureCommentVoList.add(pictureCommentVo);
        }
        Page<PictureCommentVo> pictureCommentVoPage = new Page<>();
        pictureCommentVoPage.setRecords(pictureCommentVoList);
        pictureCommentVoPage.setCurrent(page);
        pictureCommentVoPage.setSize(size);
        pictureCommentVoPage.setTotal(sortedCacheResult.getTotal());
        return pictureCommentVoPage;
    }

    private Page<PictureCommentVo> queryPictureCommentVo(PictureCommentQueryRequest pictureCommentQueryRequest,
                                                         Long current, Long size) {
        String order = pictureCommentQueryRequest.getSortOrder();
        Long pictureId = pictureCommentQueryRequest.getPictureId();
        Long targetId = pictureCommentQueryRequest.getTargetId();
        String sortedKey = getSortedKey(order, pictureId, targetId);
        String sortedTotalKey = getSortedTotalKey(pictureId, targetId);
        // 1 先查评论的缓存
        Page<PictureCommentVo> pictureCommentVoPage = queryCache(sortedKey, sortedTotalKey, CacheUtils.PICTURE_COMMENT_CACHE, order, current, size);
        if(pictureCommentVoPage == null){
            String lockStr = CacheUtils.getHexLockString(pictureCommentQueryRequest);
            Object lock = lockMap.computeIfAbsent(lockStr, key -> new Object());
            synchronized (lock) {
                // 2 再次查询缓存
                pictureCommentVoPage = queryCache(sortedKey, sortedTotalKey, CacheUtils.PICTURE_COMMENT_CACHE, order, current, size);
                if(pictureCommentVoPage == null){
                    try {
                        // 3 缓存没中，查数据库
                        Page<PictureComment> pictureCommentPage = page(new Page<>(current, size),
                                getQueryWrapper(pictureCommentQueryRequest));
                        List<PictureComment> pictureCommentList = pictureCommentPage.getRecords();
                        List<PictureCommentVo> pictureCommentVoList = this.getPictureCommentVo(pictureCommentList);
                        pictureCommentVoPage = new Page<>();
                        pictureCommentVoPage.setRecords(pictureCommentVoList);
                        pictureCommentVoPage.setCurrent(pictureCommentPage.getCurrent());
                        pictureCommentVoPage.setSize(size);
                        pictureCommentVoPage.setTotal(pictureCommentPage.getTotal());
                        // 4 写入缓存
                        pictureCommentVoList.forEach(pictureCommentVo -> {
                            Double score = (double)pictureCommentVo.getCreateTime().getTime();
                            cacheManager.zSetAdd(sortedKey, pictureCommentVo.getId(), score);
                            cacheManager.putValueToCache(CacheUtils.getPictureCommentCacheKey(pictureCommentVo.getId().toString()),
                                    JSONUtil.toJsonStr(pictureCommentVo), cacheManager.getRedisZSetExpireTime());
                        });
                        cacheManager.putValueToCache(sortedTotalKey, pictureCommentPage.getTotal(), cacheManager.getRedisZSetExpireTime());
                    } catch (Exception e) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR);
                    } finally {
                        // 防止内存泄漏
                        lockMap.remove(lockStr);
                    }
                }else {
                    // 防止内存泄漏
                    lockMap.remove(lockStr);
                }
            }
        }
        return pictureCommentVoPage;
    }

    @Override
    public Page<PictureCommentVo> getPictureCommentVo(PictureCommentQueryRequest pictureCommentQueryRequest) {
        ThrowUtils.throwIf(pictureCommentQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "pictureCommentQueryRequest为空");
        ThrowUtils.throwIf(pictureCommentQueryRequest.getPictureId() == null,
                ErrorCode.PARAMS_ERROR, "pictureId为空");
        ThrowUtils.throwIf(pictureCommentQueryRequest.getTargetId() == null,
                ErrorCode.PARAMS_ERROR, "targetId为空");
        long current = pictureCommentQueryRequest.getCurrent();
        long size = pictureCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        return queryPictureCommentVo(pictureCommentQueryRequest, current, size);
    }

    @Override
    public void canalHandlePictureComment(List<CanalHandleVo> canalHandleVoList) {
        canalHandleVoList.forEach(canalHandleVo -> {
            CanalEntry.EventType eventType = canalHandleVo.getEventType();
            PictureComment pictureComment = JSONUtil.toBean(canalHandleVo.getJsonDataStr(), PictureComment.class);
            if (eventType == CanalEntry.EventType.DELETE || cacheManager.getEntry_DELETE_FLAG().equals(pictureComment.getIsDelete())) {
                canalDeleteHandle(canalHandleVo);
            }else if (eventType == CanalEntry.EventType.UPDATE) {
                canalUpdateHandle(canalHandleVo);
            }else {
                canalInsertHandle(canalHandleVo);
            }
        });
    }

    private void canalInsertHandle(CanalHandleVo canalHandleVo){
        PictureComment pictureComment = JSONUtil.toBean(canalHandleVo.getJsonDataStr(), PictureComment.class);
        String ascSortedKey = getSortedKey(CacheUtils.ASC, pictureComment.getPictureId(), pictureComment.getTargetId());
        String descSortedKey = getSortedKey(CacheUtils.DESC, pictureComment.getPictureId(), pictureComment.getTargetId());
        String totalKey = getSortedTotalKey(pictureComment.getPictureId(), pictureComment.getTargetId());

        // 更新缓存
        Double score = (double) pictureComment.getCreateTime().getTime();
        String valueKey = CacheUtils.getPictureCommentCacheKey(pictureComment.getId().toString());
        List<PictureComment> pictureCommentList = new ArrayList<>();
        pictureCommentList.add(pictureComment);
        List<PictureCommentVo> pictureCommentVoList = this.getPictureCommentVo(pictureCommentList);
        String valueStr = JSONUtil.toJsonStr(pictureCommentVoList.getFirst());
        // 更新 total
        Long total = cacheManager.getTotal(totalKey);
        if(total == null){
            return;
        }

        cacheManager.putValueToCache(totalKey, total + 1, cacheManager.getRedisZSetExpireTime());
        cacheManager.insertSortedValue(ascSortedKey, pictureComment.getId(), score, valueKey, valueStr);
        cacheManager.insertSortedValue(descSortedKey, pictureComment.getId(), score, valueKey, valueStr);
    }

    private void canalUpdateHandle(CanalHandleVo canalHandleVo){
        PictureComment pictureComment = JSONUtil.toBean(canalHandleVo.getJsonDataStr(), PictureComment.class);
        String valueKey = CacheUtils.getPictureCommentCacheKey(pictureComment.getId().toString());
        if(!redisTemplate.hasKey(RedisKeyUtil.buildRedisKey(valueKey))){
            return;
        }
        Object Value = cacheManager.getValueCache(valueKey);
        PictureCommentVo pictureCommentVo = JSONUtil.toBean((String) Value, PictureCommentVo.class);
        pictureCommentVo.setContent(pictureComment.getContent());
        cacheManager.putValueToCache(valueKey, JSONUtil.toJsonStr(pictureCommentVo), cacheManager.getRedisZSetExpireTime());
    }

    private void canalDeleteHandle(CanalHandleVo canalHandleVo){
        PictureComment pictureComment = JSONUtil.toBean(canalHandleVo.getJsonDataStr(), PictureComment.class);
        String ascSortedKey = getSortedKey(CacheUtils.ASC, pictureComment.getPictureId(), pictureComment.getTargetId());
        String descSortedKey = getSortedKey(CacheUtils.DESC, pictureComment.getPictureId(), pictureComment.getTargetId());
        String valueKey = CacheUtils.getPictureCommentCacheKey(pictureComment.getId().toString());

        cacheManager.zSetRemove(ascSortedKey, pictureComment.getId());
        cacheManager.zSetRemove(descSortedKey, pictureComment.getId());
        cacheManager.removeValueCache(valueKey);

        String totalKey = getSortedTotalKey(pictureComment.getPictureId(), pictureComment.getTargetId());
        // 更新 total
        Long total = cacheManager.getTotal(totalKey);
        if(total == null){
            return;
        }

        cacheManager.putValueToCache(totalKey, total - 1, cacheManager.getRedisZSetExpireTime());
    }

    private String getSortedKey(String order, Long pictureId, Long targetId){
        if(targetId == null){
            return CacheUtils.getPictureSortedCommentCacheKey(order, pictureId);
        }else{
            return CacheUtils.getPictureSecondCommentSortedCache(order, targetId);
        }
    }

    private String getSortedTotalKey(Long pictureId, Long targetId){
        if(targetId == null){
            return CacheUtils.getPictureCommentSortedTotalCache(pictureId);
        }else{
            return CacheUtils.getPictureSecondCommentSortedTotalCache(targetId);
        }
    }

    @Override
    public PictureComment getById(Long pictureCommentId) {
        return super.getById(pictureCommentId);
    }
}
