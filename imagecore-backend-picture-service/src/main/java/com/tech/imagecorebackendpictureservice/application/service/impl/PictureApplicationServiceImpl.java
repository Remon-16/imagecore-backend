package com.tech.imagecorebackendpictureservice.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.tech.imagecorebackendpictureservice.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendcommon.utils.CacheUtils;
import com.tech.imagecorebackendmodel.auth.SpaceUserPermissionConstant;
import com.tech.imagecorebackendmodel.dto.picture.*;
import com.tech.imagecorebackendmodel.dto.space.analyze.*;
import com.tech.imagecorebackendmodel.dto.space.inner.PermissionListRequest;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceUserAuthRequest;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.picture.valueobject.PictureReviewStatusEnum;
import com.tech.imagecorebackendmodel.space.entity.Space;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.picture.PictureVO;
import com.tech.imagecorebackendmodel.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.CreatePictureOutPaintingTaskRequest;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.Text2ImageTaskRequest;
import com.tech.imagecorebackendpictureservice.api.aliyunai.model.Text2ImageTaskResponse;
import com.tech.imagecorebackendpictureservice.application.service.PictureApplicationService;
import com.tech.imagecorebackendpictureservice.domain.picture.service.PictureDomainService;
import com.tech.imagecorebackendpictureservice.domain.picture.service.ThumbDomainService;
import com.tech.imagecorebackendpictureservice.infrastructure.dco.CacheManager;
import com.tech.imagecorebackendpictureservice.infrastructure.mapper.PictureMapper;
import com.tech.imagecorebackendserviceclient.application.service.SpaceFeignClient;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Remon
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-11 20:45:51
 */
@Slf4j
@Service
public class PictureApplicationServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureApplicationService {

    @Resource
    private PictureDomainService pictureDomainService;

    @Resource
    private UserFeignClient userApplicationService;

    @Resource
    private ThumbDomainService thumbDomainService;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private SpaceFeignClient spaceFeignClient;

    @Override
    public void validPicture(Picture picture) {
        if (picture == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        picture.validPicture();
    }

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        return pictureDomainService.uploadPicture(inputSource, pictureUploadRequest, loginUser);
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userApplicationService.getUserById(userId);
            UserVO userVO = userApplicationService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userApplicationService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userApplicationService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        return pictureDomainService.getQueryWrapper(pictureQueryRequest);
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        pictureDomainService.doPictureReview(pictureReviewRequest, loginUser);
    }

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        pictureDomainService.fillReviewParams(picture, loginUser);
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        return pictureDomainService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        pictureDomainService.clearPictureFile(oldPicture);
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        pictureDomainService.deletePicture(pictureId, loginUser);
    }

    @Override
    public void editPicture(Picture picture, User loginUser) {
        pictureDomainService.editPicture(picture, loginUser);
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        pictureDomainService.checkPictureAuth(loginUser, picture);
    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        return pictureDomainService.searchPictureByColor(spaceId, picColor, loginUser);
    }

    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        pictureDomainService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        return pictureDomainService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
    }

    private List<PictureVO> unLoginSetThumbState(List<PictureVO> pictureVOList){
        pictureVOList.forEach(pictureVO -> {
            pictureVO.setHasThumb(Boolean.FALSE);
        });
        return pictureVOList;
    }

    private List<PictureVO> setNewThumbCount(List<PictureVO> pictureVOList){
        List<Long> pictureIdList = pictureVOList.stream()
                .map(PictureVO::getId).collect(Collectors.toList());
        Map<Long, Long> pictureIdThumbCountMap = cacheManager.getThumbCountCache(pictureIdList);
        for (PictureVO pictureVO : pictureVOList) {
            if (pictureIdThumbCountMap.containsKey(pictureVO.getId())) {
                pictureVO.setThumbCount(pictureIdThumbCountMap.get(pictureVO.getId()));
            }
        }
        return pictureVOList;
    }

    private List<PictureVO> setThumbState(List<PictureVO> pictureVOList, User loginUser){
        if (ObjUtil.isEmpty(loginUser)) {
            pictureVOList.forEach(pictureVO -> {
                pictureVO.setHasThumb(Boolean.FALSE);
            });
            return pictureVOList;
        }else {
            return thumbDomainService.getPictureThumbState(pictureVOList, loginUser);
        }
    }

    @Override
    public Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        User loginUser = userApplicationService.getUserFromRequest(request);
        // 1 先从缓存中查图片的数据
        String queryKey = CacheUtils.getPictureQueryCacheKey(pictureQueryRequest);

        Object queryValue = cacheManager.getValueCache(queryKey);
        Page<PictureVO> pictureVOPage = null;
        List<PictureVO> pictureVOList = null;
        if (queryValue != null) {
            // 1.2 如果缓存命中，根据图片的数据，获取图片点赞数量
            pictureVOPage = JSONUtil.toBean(
                    (String) queryValue,
                    new TypeReference<Page<PictureVO>>() {}, // 指定完整泛型结构
                    false // 是否忽略转换错误
            );
            // 1.3 如果缓存命中，如果用户登录了，从缓存中获取用户点赞的数据
            pictureVOList = setNewThumbCount(pictureVOPage.getRecords());
            pictureVOList = setThumbState(pictureVOList, loginUser);
            pictureVOPage.setRecords(pictureVOList);
            // 1.4 返回结果
            return pictureVOPage;
        }
        // 2.1 如果缓存没有命中，直接从数据库里查询
        Page<Picture> picturePage = page(new Page<>(current, size),
                getQueryWrapper(pictureQueryRequest));
        pictureVOPage = getPictureVOPage(picturePage, request);
        pictureVOList = pictureVOPage.getRecords();
        pictureVOList = setThumbState(pictureVOList, loginUser);
        // 2.2 尝试从缓存中获取最新的点赞数量
        pictureVOPage.setRecords(setNewThumbCount(pictureVOPage.getRecords()));
        // 2.3 写入 Redis
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        cacheManager.putValueToCache(queryKey, cacheValue);
        // 2.4 返回结果
        return pictureVOPage;
    }

    @Override
    public PictureVO getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 1. 查询缓存
        PictureVO pictureVO = null;
        Space space = null;
        List<String> permissionList = null;
        Boolean userHasThumb = null;
        String pictureQueryCacheKey = CacheUtils.getSinglePictureQueryCacheKey(id);
        Object queryValue = cacheManager.getValueCache(pictureQueryCacheKey);
        User loginUser = userApplicationService.getUserFromRequest(request);
        if (queryValue != null) {
            pictureVO = JSONUtil.toBean((String) queryValue, PictureVO.class);;
            String pictureThumbCountKey = CacheUtils.getPictureCacheKey(String.valueOf(id));
            Object thumbCountValue = cacheManager.getValueCache(pictureThumbCountKey);
            if (thumbCountValue != null) {
                pictureVO.setThumbCount(Long.parseLong(thumbCountValue.toString()));
            }
            userHasThumb = thumbDomainService.hasThumb(pictureVO.getId(), loginUser.getId());
            pictureVO.setHasThumb(userHasThumb);
            space = pictureVO.getSpace();
            PermissionListRequest permissionListRequest = new PermissionListRequest();
            permissionListRequest.setSpace(space);
            permissionListRequest.setLoginUser(loginUser);
            permissionList = spaceFeignClient.getPermissionList(permissionListRequest);
            pictureVO.setPermissionList(permissionList);
            return pictureVO;
        }
        // 2. 缓存没命中，查询数据库
        Picture picture = getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            Space space1 = spaceFeignClient.getById(spaceId);
            SpaceUserAuthRequest spaceUserAuthRequest = new SpaceUserAuthRequest();
            spaceUserAuthRequest.setSpace(space1);
            spaceUserAuthRequest.setLoginUser(loginUser);
            spaceUserAuthRequest.setNeedPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!spaceFeignClient.hasPermission(spaceUserAuthRequest),
                    ErrorCode.OPERATION_ERROR, "无查询权限");
            space = spaceFeignClient.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 获取权限列表
        PermissionListRequest permissionListRequest = new PermissionListRequest();
        permissionListRequest.setSpace(space);
        permissionListRequest.setLoginUser(loginUser);
        permissionList = spaceFeignClient.getPermissionList(permissionListRequest);
        pictureVO = getPictureVO(picture, request);
        pictureVO.setPermissionList(permissionList);
        pictureVO.setSpace(space);
        userHasThumb = thumbDomainService.hasThumb(pictureVO.getId(), loginUser.getId());
        pictureVO.setHasThumb(userHasThumb);
        // 存缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVO);
        cacheManager.putValueToCache(pictureQueryCacheKey, cacheValue);
        return pictureVO;
    }

    /**
     * 根据请求对象封装查询条件
     *
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        // 全空间分析
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryAll) {
            return;
        }
        // 公共图库
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }
        // 分析特定空间
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }

    @Override
    public List<Object> getPictureObjList(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest) {
        // 统计图库的使用空间
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("picSize");
        // 补充查询范围
        fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
        return this.getBaseMapper().selectObjs(queryWrapper);
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest) {
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);

        // 使用 MyBatis Plus 分组查询
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize")
                .groupBy("category");

        // 查询并转换结果
        return this.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result -> {
                    String category = (String) result.get("category");
                    Long count = ((Number) result.get("count")).longValue();
                    Long totalSize = ((Number) result.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getTagsJson(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest) {
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        // 查询所有符合条件的标签
        queryWrapper.select("tags");
        return this.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getPicSizeList(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest) {
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);

        // 查询所有符合条件的图片大小
        queryWrapper.select("picSize");
        // 100、120、1000
        return this.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(size -> (Long) size)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> querySpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest) {
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        // 补充用户 id 查询
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        // 补充分析维度：每日、每周、每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime) as period", "count(*) as count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }

        // 分组排序
        queryWrapper.groupBy("period").orderByAsc("period");

        // 查询并封装结果
        return this.getBaseMapper().selectMaps(queryWrapper);
    }

    @Override
    public Text2ImageTaskResponse createText2ImageTask(Text2ImageTaskRequest text2ImageTaskRequest) {
        return pictureDomainService.createText2ImageTask(text2ImageTaskRequest);
    }

    @Override
    public String uploadUserAvatar(MultipartFile multipartFile, String uploadPathPrefix) {
        return pictureDomainService.uploadUserAvatar(multipartFile, uploadPathPrefix);
    }
}
