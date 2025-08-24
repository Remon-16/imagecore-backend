package com.tech.imagecorebackendpictureservice.interfaces.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendcommon.annotation.AddScore;
import com.tech.imagecorebackendcommon.annotation.AuthCheck;
import com.tech.imagecorebackendcommon.annotation.DeductScore;
import com.tech.imagecorebackendpictureservice.api.aliyunai.AliYunAiApi;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.*;
import com.tech.imagecorebackendpictureservice.api.imagesearch.ImageSearchApiFacade;
import com.tech.imagecorebackendpictureservice.api.imagesearch.model.ImageSearchResult;
import com.tech.imagecorebackendcommon.common.BaseResponse;
import com.tech.imagecorebackendcommon.common.DeleteRequest;
import com.tech.imagecorebackendcommon.common.ResultUtils;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendmodel.auth.SpaceUserPermissionConstant;
import com.tech.imagecorebackendmodel.dto.picture.*;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceUserAuthRequest;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.picture.valueobject.PictureReviewStatusEnum;
import com.tech.imagecorebackendmodel.space.entity.Space;
import com.tech.imagecorebackendmodel.user.constant.UserConstant;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.picture.PictureTagCategory;
import com.tech.imagecorebackendmodel.vo.picture.PictureVO;
import com.tech.imagecorebackendpictureservice.application.service.PictureApplicationService;
import com.tech.imagecorebackendmodel.user.constant.UserScoreConstant;
import com.tech.imagecorebackendpictureservice.interfaces.assembler.PictureAssembler;
import com.tech.imagecorebackendserviceclient.application.service.SpaceFeignClient;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @author Remon
 */
@Slf4j
@RestController
@RequestMapping("/")
public class PictureController {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private PictureApplicationService pictureApplicationService;

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private SpaceFeignClient spaceFeignClient;


    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AddScore(type = UserScoreConstant.UPLOAD_PICTURE,
            value = 20L,
            maxCount = -1L)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userFeignClient.getLoginUser(request);
        Space space = spaceFeignClient.getById(pictureUploadRequest.getSpaceId());

        SpaceUserAuthRequest spaceUserAuthRequest = new SpaceUserAuthRequest();
        spaceUserAuthRequest.setLoginUser(loginUser);
        spaceUserAuthRequest.setSpace(space);
        spaceUserAuthRequest.setNeedPermission(SpaceUserPermissionConstant.PICTURE_UPLOAD);

        ThrowUtils.throwIf(!spaceFeignClient.hasPermission(spaceUserAuthRequest),
                ErrorCode.OPERATION_ERROR, "无上传权限");
        PictureVO pictureVO = pictureApplicationService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    @AddScore(type = UserScoreConstant.UPLOAD_PICTURE,
            value = 20L,
            maxCount = -1L)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userFeignClient.getLoginUser(request);
        Space space = spaceFeignClient.getById(pictureUploadRequest.getSpaceId());
        SpaceUserAuthRequest spaceUserAuthRequest = new SpaceUserAuthRequest();
        spaceUserAuthRequest.setLoginUser(loginUser);
        spaceUserAuthRequest.setSpace(space);
        spaceUserAuthRequest.setNeedPermission(SpaceUserPermissionConstant.PICTURE_UPLOAD);
        ThrowUtils.throwIf(!spaceFeignClient.hasPermission(spaceUserAuthRequest),
                ErrorCode.OPERATION_ERROR, "无上传权限");
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureApplicationService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/delete")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest
            , HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);
        Picture picture = pictureApplicationService.getById(deleteRequest.getId());
        Space space = spaceFeignClient.getById(picture.getSpaceId());
        SpaceUserAuthRequest spaceUserAuthRequest = new SpaceUserAuthRequest();
        spaceUserAuthRequest.setLoginUser(loginUser);
        spaceUserAuthRequest.setSpace(space);
        spaceUserAuthRequest.setNeedPermission(SpaceUserPermissionConstant.PICTURE_DELETE);
        ThrowUtils.throwIf(!spaceFeignClient.hasPermission(spaceUserAuthRequest),
                ErrorCode.OPERATION_ERROR, "无删除权限");
        pictureApplicationService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     *
     * @param pictureUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = PictureAssembler.toPictureEntity(pictureUpdateRequest);
        // 数据校验
        pictureApplicationService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureApplicationService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        User loginUser = userFeignClient.getLoginUser(request);
        pictureApplicationService.fillReviewParams(oldPicture, loginUser);
        // 操作数据库
        boolean result = pictureApplicationService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureApplicationService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        PictureVO pictureVO = pictureApplicationService.getPictureVOById(id, request);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size),
                pictureApplicationService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            // 公开图库
            // 普通用户默认只能看到审核通过的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            User loginUser = userFeignClient.getLoginUser(request);
            Space space = spaceFeignClient.getById(spaceId);
            SpaceUserAuthRequest spaceUserAuthRequest = new SpaceUserAuthRequest();
            spaceUserAuthRequest.setLoginUser(loginUser);
            spaceUserAuthRequest.setSpace(space);
            spaceUserAuthRequest.setNeedPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!spaceFeignClient.hasPermission(spaceUserAuthRequest),
                    ErrorCode.OPERATION_ERROR, "无查询权限");
        }
        // 查询数据库
        Page<Picture> picturePage = pictureApplicationService.page(new Page<>(current, size),
                pictureApplicationService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureApplicationService.getPictureVOPage(picturePage, request));
    }

    /**
     * 分页获取图片列表（封装类，有缓存）
     */
    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        Page<PictureVO> pictureVOPage = pictureApplicationService.listPictureVOByPage(pictureQueryRequest, request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);
        Picture picture = pictureApplicationService.getById(pictureEditRequest.getId());
        Space space = spaceFeignClient.getById(picture.getSpaceId());
        SpaceUserAuthRequest spaceUserAuthRequest = new SpaceUserAuthRequest();
        spaceUserAuthRequest.setLoginUser(loginUser);
        spaceUserAuthRequest.setSpace(space);
        spaceUserAuthRequest.setNeedPermission(SpaceUserPermissionConstant.PICTURE_EDIT);
        ThrowUtils.throwIf(!spaceFeignClient.hasPermission(spaceUserAuthRequest),
                ErrorCode.OPERATION_ERROR, "无编辑权限");
        // 在此处将实体类和 DTO 进行转换
        Picture pictureEntity = PictureAssembler.toPictureEntity(pictureEditRequest);
        pictureApplicationService.editPicture(pictureEntity, loginUser);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核图片
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userFeignClient.getLoginUser(request);
        pictureApplicationService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取并创建图片
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userFeignClient.getLoginUser(request);
        int uploadCount = pictureApplicationService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    /**
     * 以图搜图
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureApplicationService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(picture.getUrl());
        return ResultUtils.success(resultList);
    }

    /**
     * 按照颜色搜索
     */
    @PostMapping("/search/color")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userFeignClient.getLoginUser(request);
        Space space = spaceFeignClient.getById(spaceId);
        SpaceUserAuthRequest spaceUserAuthRequest = new SpaceUserAuthRequest();
        spaceUserAuthRequest.setLoginUser(loginUser);
        spaceUserAuthRequest.setSpace(space);
        spaceUserAuthRequest.setNeedPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
        ThrowUtils.throwIf(!spaceFeignClient.hasPermission(spaceUserAuthRequest),
                ErrorCode.OPERATION_ERROR, "无查看权限");
        List<PictureVO> pictureVOList = pictureApplicationService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 批量编辑图片
     */
    @PostMapping("/edit/batch")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userFeignClient.getLoginUser(request);

        Picture picture = pictureApplicationService.getById(pictureEditByBatchRequest.getSpaceId());
        Space space = spaceFeignClient.getById(picture.getSpaceId());
        SpaceUserAuthRequest spaceUserAuthRequest = new SpaceUserAuthRequest();
        spaceUserAuthRequest.setLoginUser(loginUser);
        spaceUserAuthRequest.setSpace(space);
        spaceUserAuthRequest.setNeedPermission(SpaceUserPermissionConstant.PICTURE_EDIT);
        ThrowUtils.throwIf(!spaceFeignClient.hasPermission(spaceUserAuthRequest),
                ErrorCode.OPERATION_ERROR, "无编辑权限");
        pictureApplicationService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 创建 AI 扩图任务
     */
    @PostMapping("/out_painting/create_task")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
                                                                                    HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);

        Picture picture = pictureApplicationService.getById(createPictureOutPaintingTaskRequest.getPictureId());
        Space space = spaceFeignClient.getById(picture.getSpaceId());
        SpaceUserAuthRequest spaceUserAuthRequest = new SpaceUserAuthRequest();
        spaceUserAuthRequest.setLoginUser(loginUser);
        spaceUserAuthRequest.setSpace(space);
        spaceUserAuthRequest.setNeedPermission(SpaceUserPermissionConstant.PICTURE_EDIT);
        ThrowUtils.throwIf(!spaceFeignClient.hasPermission(spaceUserAuthRequest),
                ErrorCode.OPERATION_ERROR, "无编辑权限");
        CreateOutPaintingTaskResponse response = pictureApplicationService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }

    /**
     * 文生图任务
     * @return
     */
    @DeductScore(type = UserScoreConstant.TEXT_TO_IMAGE,
            value = -10L,
            maxCount = 10L)
    @PostMapping("/text_gen/create_task")
    public BaseResponse<Text2ImageTaskResponse> createPictureByText(Text2ImageTaskRequest text2ImageTaskRequest, HttpServletRequest request) {
        if (text2ImageTaskRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Text2ImageTaskResponse text2ImageTaskResponse = pictureApplicationService.createText2ImageTask(text2ImageTaskRequest);
        return ResultUtils.success(text2ImageTaskResponse);
    }
}
