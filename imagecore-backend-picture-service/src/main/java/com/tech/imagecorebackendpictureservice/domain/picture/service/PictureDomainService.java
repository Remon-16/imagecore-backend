package com.tech.imagecorebackendpictureservice.domain.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.tech.imagecorebackendmodel.dto.picture.*;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.picture.PictureVO;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.CreatePictureOutPaintingTaskRequest;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.Text2ImageTaskRequest;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.Text2ImageTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Remon
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2024-12-11 20:45:51
 */
public interface PictureDomainService {

    /**
     * 上传图片
     *
     * @param inputSource          文件输入源
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                 User loginUser);

    /**
     * 清理图片文件
     *
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 删除图片
     *
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片
     *
     * @param picture
     * @param loginUser
     */
    void editPicture(Picture picture, User loginUser);

    /**
     * 校验空间图片的权限
     *
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片
     *
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建扩图任务
     *
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

    /**
     * 文生图接口
     * @param text2ImageTaskRequest
     * @return
     */
    Text2ImageTaskResponse createText2ImageTask(Text2ImageTaskRequest text2ImageTaskRequest);

    String uploadUserAvatar(MultipartFile multipartFile, String uploadPathPrefix);
}
