package com.tech.imagecorebackendpictureservice.interfaces.controller;

import com.tech.imagecorebackendcommon.common.BaseResponse;
import com.tech.imagecorebackendcommon.common.ResultUtils;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentQueryRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentRequest;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentRootVo;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;
import com.tech.imagecorebackendpictureservice.application.service.PictureCommentApplicationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Remon
 */
@Slf4j
@RestController
@RequestMapping("/picture_comment")
public class PictureCommentController {

    @Resource
    private PictureCommentApplicationService pictureCommentApplicationService;

    @PostMapping("/saveOrUpdate")
    public BaseResponse<Boolean> saveOrUpdatePictureComment(PictureCommentRequest pictureCommentRequest) {
        if (pictureCommentRequest == null || pictureCommentRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        pictureCommentApplicationService.saveOrUpdatePictureComment(pictureCommentRequest);
        return ResultUtils.success(true);
    }

    @PostMapping("/query_root")
    public BaseResponse<List<PictureCommentRootVo>> getPictureCommentRootVo(PictureCommentQueryRequest pictureCommentQueryRequest){
        if (pictureCommentQueryRequest == null || pictureCommentQueryRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<PictureCommentRootVo> pictureCommentRootVoList = pictureCommentApplicationService.getPictureCommentRootVo(pictureCommentQueryRequest);
        return ResultUtils.success(pictureCommentRootVoList);
    }

    @PostMapping("/query_second")
    public BaseResponse<List<PictureCommentVo>> getPictureCommentVo(PictureCommentQueryRequest pictureCommentQueryRequest){
        if (pictureCommentQueryRequest == null || pictureCommentQueryRequest.getPictureId() == null || pictureCommentQueryRequest.getTargetId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<PictureCommentVo> pictureCommentVoList = pictureCommentApplicationService.getPictureCommentVo(pictureCommentQueryRequest);
        return ResultUtils.success(pictureCommentVoList);
    }
}
