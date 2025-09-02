package com.tech.imagecorebackendpictureservice.domain.picture.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentQueryRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentRequest;
import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentRootVo;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import com.tech.imagecorebackendpictureservice.domain.picture.service.PictureCommentDomainService;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.PictureCommentMapper;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PictureCommentDomainServiceImpl extends ServiceImpl<PictureCommentMapper, PictureComment>
        implements PictureCommentDomainService {

    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public PictureComment saveOrUpdatePictureComment(PictureCommentRequest pictureCommentRequest) {
        ThrowUtils.throwIf(pictureCommentRequest == null,
                ErrorCode.PARAMS_ERROR, "pictureCommentRequest为空");
        PictureComment pictureComment = new PictureComment();
        BeanUtils.copyProperties(pictureCommentRequest, pictureComment);
        if (pictureComment.getId() == null) {
            this.save(pictureComment);
        }else {
            this.updateById(pictureComment);
        }
        return pictureComment;
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
        Map<Long, List<User>> userIdUserListMap = userFeignClient.listByIds(userIdSet).stream()
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
    public List<PictureCommentRootVo> getPictureCommentRootVo(PictureCommentQueryRequest pictureCommentQueryRequest) {
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
        Page<PictureComment> pictureCommentPage = page(new Page<>(current, size),
                getQueryWrapper(pictureCommentQueryRequest));
        List<PictureComment> pictureCommentList = pictureCommentPage.getRecords();

        List<PictureCommentVo> pictureCommentVoList = this.getPictureCommentVo(pictureCommentList);

        // 接着通过评论的id，去查前10个子评论
        List<PictureCommentRootVo> pictureCommentRootVoList = new ArrayList<>();
        for(PictureCommentVo pictureCommentVo : pictureCommentVoList){
            PictureCommentQueryRequest pictureCommentQueryRequest1 = new PictureCommentQueryRequest();
            pictureCommentQueryRequest1.setCurrent(1);
            pictureCommentQueryRequest1.setPageSize(10);
            pictureCommentQueryRequest1.setTargetId(pictureCommentVo.getId());
            pictureCommentQueryRequest1.setPictureId(pictureCommentVo.getPictureId());

            List<PictureCommentVo> pictureCommentVoList2 = this.getPictureCommentVo(pictureCommentQueryRequest1);

            PictureCommentRootVo pictureCommentRootVo = new PictureCommentRootVo();
            BeanUtils.copyProperties(pictureCommentVo, pictureCommentRootVo);
            pictureCommentRootVo.setPictureCommentVos(pictureCommentVoList2);
            pictureCommentRootVoList.add(pictureCommentRootVo);
        }
        return pictureCommentRootVoList;
    }

    @Override
    public List<PictureCommentVo> getPictureCommentVo(PictureCommentQueryRequest pictureCommentQueryRequest) {
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
        Page<PictureComment> pictureCommentPage = page(new Page<>(current, size),
                getQueryWrapper(pictureCommentQueryRequest));
        List<PictureComment> pictureCommentList = pictureCommentPage.getRecords();
        return this.getPictureCommentVo(pictureCommentList);
    }
}
