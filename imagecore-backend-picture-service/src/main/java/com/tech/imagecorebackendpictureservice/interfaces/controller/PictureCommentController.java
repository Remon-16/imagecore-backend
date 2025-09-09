package com.tech.imagecorebackendpictureservice.interfaces.controller;

import com.tech.imagecorebackendcommon.common.BaseResponse;
import com.tech.imagecorebackendcommon.common.ResultUtils;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentQueryRequest;
import com.tech.imagecorebackendmodel.dto.picture.PictureCommentRequest;
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
    public BaseResponse<Boolean> saveOrUpdatePictureComment(@RequestBody PictureCommentRequest pictureCommentRequest, HttpServletRequest request) {
        if (pictureCommentRequest == null || pictureCommentRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 编辑需要校验用户
        if(pictureCommentRequest.getId() != null) {
            User loginUser = userFeignClient.getUserFromRequest(request);
            if (loginUser == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            if(!loginUser.getId().equals(pictureCommentRequest.getUserId())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID与评论ID不一致！");
            }
        }
        pictureCommentApplicationService.saveOrUpdatePictureComment(pictureCommentRequest);
        return ResultUtils.success(true);
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
    public BaseResponse<List<PictureCommentRootVo>> getPictureCommentRootVo(@RequestBody PictureCommentQueryRequest pictureCommentQueryRequest){
        if (pictureCommentQueryRequest == null || pictureCommentQueryRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<PictureCommentRootVo> pictureCommentRootVoList = pictureCommentApplicationService.getPictureCommentRootVo(pictureCommentQueryRequest);
        return ResultUtils.success(pictureCommentRootVoList);
    }

    @PostMapping("/query_second")
    public BaseResponse<List<PictureCommentVo>> getPictureCommentVo(@RequestBody PictureCommentQueryRequest pictureCommentQueryRequest){
        if (pictureCommentQueryRequest == null || pictureCommentQueryRequest.getPictureId() == null || pictureCommentQueryRequest.getTargetId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<PictureCommentVo> pictureCommentVoList = pictureCommentApplicationService.getPictureCommentVo(pictureCommentQueryRequest);
        return ResultUtils.success(pictureCommentVoList);
    }
}
