package com.tech.imagecorebackendspaceservice.interfaces.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tech.imagecorebackendcommon.common.BaseResponse;
import com.tech.imagecorebackendcommon.common.DeleteRequest;
import com.tech.imagecorebackendcommon.common.ResultUtils;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendmodel.auth.SpaceUserPermissionConstant;
import com.tech.imagecorebackendmodel.dto.spaceuser.SpaceUserAddRequest;
import com.tech.imagecorebackendmodel.dto.spaceuser.SpaceUserEditRequest;
import com.tech.imagecorebackendmodel.dto.spaceuser.SpaceUserQueryRequest;
import com.tech.imagecorebackendmodel.space.entity.Space;
import com.tech.imagecorebackendmodel.space.entity.SpaceUser;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.space.SpaceUserVO;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import com.tech.imagecorebackendspaceservice.application.service.SpaceApplicationService;
import com.tech.imagecorebackendspaceservice.application.service.SpaceUserApplicationService;
import com.tech.imagecorebackendspaceservice.auth.SpaceUserAuthManager;
import com.tech.imagecorebackendspaceservice.interfaces.assembler.SpaceUserAssembler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间成员管理
 */
@RestController
@RequestMapping("/spaceUser")
@Slf4j
public class SpaceUserController {

    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;

    @Resource
    private UserFeignClient userApplicationService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    private SpaceApplicationService spaceApplicationService;

    /**
     * 添加成员到空间
     */
    @PostMapping("/add")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);

        User loginUser = userApplicationService.getLoginUser(request);
        Space space = spaceApplicationService.getById(spaceUserAddRequest.getSpaceId());
        ThrowUtils.throwIf(!spaceUserAuthManager.hasPermission(space, loginUser, SpaceUserPermissionConstant.SPACE_USER_MANAGE),
                ErrorCode.OPERATION_ERROR, "无空间添加成员权限");
        long id = spaceUserApplicationService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(id);
    }

    /**
     * 从空间移除成员
     */
    @PostMapping("/delete")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest,
                                                 HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        SpaceUser oldSpaceUser = spaceUserApplicationService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userApplicationService.getLoginUser(request);
        Space space = spaceApplicationService.getById(oldSpaceUser.getSpaceId());
        ThrowUtils.throwIf(!spaceUserAuthManager.hasPermission(space, loginUser, SpaceUserPermissionConstant.SPACE_USER_MANAGE),
                ErrorCode.OPERATION_ERROR, "无空间成员删除权限");
        // 操作数据库
        boolean result = spaceUserApplicationService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询某个成员在某个空间的信息
     */
    @PostMapping("/get")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);

        User loginUser = userApplicationService.getLoginUser(request);
        Space space = spaceApplicationService.getById(spaceId);
        ThrowUtils.throwIf(!spaceUserAuthManager.hasPermission(space, loginUser, SpaceUserPermissionConstant.SPACE_USER_MANAGE),
                ErrorCode.OPERATION_ERROR, "无空间查询权限");
        // 查询数据库
        SpaceUser spaceUser = spaceUserApplicationService.getOne(spaceUserApplicationService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询成员信息列表
     */
    @PostMapping("/list")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);

        User loginUser = userApplicationService.getLoginUser(request);
        Space space = spaceApplicationService.getById(spaceUserQueryRequest.getSpaceId());
        ThrowUtils.throwIf(!spaceUserAuthManager.hasPermission(space, loginUser, SpaceUserPermissionConstant.SPACE_USER_MANAGE),
                ErrorCode.OPERATION_ERROR, "无空间查询权限");
        List<SpaceUser> spaceUserList = spaceUserApplicationService.list(
                spaceUserApplicationService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserApplicationService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 编辑成员信息（设置权限）
     */
    @PostMapping("/edit")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest,
                                               HttpServletRequest request) {
        if (spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        SpaceUser spaceUser = SpaceUserAssembler.toSpaceUserEntity(spaceUserEditRequest);
        // 数据校验
        spaceUserApplicationService.validSpaceUser(spaceUser, false);
        // 判断是否存在
        long id = spaceUserEditRequest.getId();
        SpaceUser oldSpaceUser = spaceUserApplicationService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userApplicationService.getLoginUser(request);
        Space space = spaceApplicationService.getById(oldSpaceUser.getSpaceId());
        ThrowUtils.throwIf(!spaceUserAuthManager.hasPermission(space, loginUser, SpaceUserPermissionConstant.SPACE_USER_MANAGE),
                ErrorCode.OPERATION_ERROR, "无空间成员编辑权限");
        // 操作数据库
        boolean result = spaceUserApplicationService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询我加入的团队空间列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserApplicationService.list(
                spaceUserApplicationService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserApplicationService.getSpaceUserVOList(spaceUserList));
    }
}