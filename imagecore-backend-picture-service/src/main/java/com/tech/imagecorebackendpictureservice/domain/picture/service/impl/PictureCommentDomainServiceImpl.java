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
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentRootVo;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;
import com.tech.imagecorebackendpictureservice.domain.picture.service.PictureCommentDomainService;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.PictureCommentMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PictureCommentDomainServiceImpl extends ServiceImpl<PictureCommentMapper, PictureComment>
        implements PictureCommentDomainService {

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
        // 接着通过评论的id，去查前10个子评论
        List<PictureCommentRootVo> pictureCommentRootVoList = new ArrayList<>();
        for(PictureComment pictureComment : pictureCommentList){
            PictureCommentQueryRequest pictureCommentQueryRequest1 = new PictureCommentQueryRequest();
            pictureCommentQueryRequest1.setCurrent(1);
            pictureCommentQueryRequest1.setPageSize(10);
            pictureCommentQueryRequest1.setTargetId(pictureComment.getId());
            pictureCommentQueryRequest1.setPictureId(pictureComment.getPictureId());

            List<PictureCommentVo> pictureCommentVoList = this.getPictureCommentVo(pictureCommentQueryRequest1);
            PictureCommentRootVo pictureCommentRootVo = new PictureCommentRootVo();
            BeanUtils.copyProperties(pictureComment, pictureCommentRootVo);
            pictureCommentRootVo.setPictureCommentVos(pictureCommentVoList);
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
        return pictureCommentList.stream().map(PictureCommentVo::objToVo).collect(Collectors.toList());
    }
}
