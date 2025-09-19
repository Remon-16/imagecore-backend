package com.tech.imagecorebackendpictureservice.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendcommon.common.BaseResponse;
import com.tech.imagecorebackendcommon.common.ResultUtils;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentQueryRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentRequest;
import com.tech.imagecorebackendmodel.picture.entity.PictureComment;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentRootVo;
import com.tech.imagecorebackendmodel.vo.picture.PictureCommentVo;
import com.tech.imagecorebackendpictureservice.application.service.PictureCommentApplicationService;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Resource
    private UserFeignClient userFeignClient;

    @PostMapping("/saveOrUpdate")
    public BaseResponse<PictureCommentVo> saveOrUpdatePictureComment(@RequestBody PictureCommentRequest pictureCommentRequest, HttpServletRequest request) {
        if (pictureCommentRequest == null || pictureCommentRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getUserFromRequest(request);
        // 编辑需要校验用户
        if(pictureCommentRequest.getId() != null) {
            if (loginUser == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            if(!loginUser.getId().equals(pictureCommentRequest.getUserId())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID与评论ID不一致！");
            }
        }
        PictureComment pictureComment = pictureCommentApplicationService.saveOrUpdatePictureComment(pictureCommentRequest);
        return ResultUtils.success(PictureCommentVo.objToVo(pictureComment));
    }


    @PostMapping("/deleteComment")
    public BaseResponse<Boolean> deleteComment(@RequestBody PictureCommentRequest pictureCommentRequest, HttpServletRequest request) {
        if (pictureCommentRequest == null || pictureCommentRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getUserFromRequest(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(!loginUser.getId().equals(pictureCommentRequest.getUserId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID与评论ID不一致！");
        }
        pictureCommentApplicationService.deletePictureComment(pictureCommentRequest);
        return ResultUtils.success(true);
    }

    @PostMapping("/query_root")
    public BaseResponse<Page<PictureCommentRootVo>> getPictureCommentRootVo(@RequestBody PictureCommentQueryRequest pictureCommentQueryRequest){
        if (pictureCommentQueryRequest == null || pictureCommentQueryRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<PictureCommentRootVo> pictureCommentRootVoPage = pictureCommentApplicationService.getPictureCommentRootVo(pictureCommentQueryRequest);
        return ResultUtils.success(pictureCommentRootVoPage);
    }

    @PostMapping("/query_second")
    public BaseResponse<Page<PictureCommentVo>> getPictureCommentVo(@RequestBody PictureCommentQueryRequest pictureCommentQueryRequest){
        if (pictureCommentQueryRequest == null || pictureCommentQueryRequest.getPictureId() == null || pictureCommentQueryRequest.getTargetId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<PictureCommentVo> pictureCommentVoPage = pictureCommentApplicationService.getPictureCommentVo(pictureCommentQueryRequest);
        return ResultUtils.success(pictureCommentVoPage);
    }
}
