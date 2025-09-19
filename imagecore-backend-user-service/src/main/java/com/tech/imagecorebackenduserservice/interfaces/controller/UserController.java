package com.tech.imagecorebackenduserservice.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.imagecorebackendcommon.annotation.AuthCheck;
import com.tech.imagecorebackendcommon.annotation.DeductScore;
import com.tech.imagecorebackendcommon.common.BaseResponse;
import com.tech.imagecorebackendcommon.common.DeleteRequest;
import com.tech.imagecorebackendcommon.common.ResultUtils;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendmodel.dto.user.*;
import com.tech.imagecorebackendmodel.user.constant.MessageConstant;
import com.tech.imagecorebackendmodel.user.constant.UserConstant;
import com.tech.imagecorebackendmodel.user.constant.UserScoreConstant;
import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.user.valueobject.MessageType;
import com.tech.imagecorebackendmodel.vo.user.LoginUserVO;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import com.tech.imagecorebackenduserservice.application.service.MessageApplicationService;
import com.tech.imagecorebackenduserservice.application.service.UserApplicationService;
import com.tech.imagecorebackenduserservice.interfaces.assembler.UserAssembler;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/")
public class UserController {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private MessageApplicationService messageApplicationService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = userApplicationService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userApplicationService.userLogin(userLoginRequest, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        return ResultUtils.success(userApplicationService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userApplicationService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User userEntity = UserAssembler.toUserEntity(userAddRequest);
        return ResultUtils.success(userApplicationService.saveUser(userEntity));
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userApplicationService.getUserById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userApplicationService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userApplicationService.deleteUser(deleteRequest);
        return ResultUtils.success(b);
    }

    /**
     * 用户上传头像
     * @return
     */
    @PostMapping("/update_user_avatar")
    public BaseResponse<Boolean> updateUserAvatar(
            @RequestPart("file") MultipartFile multipartFile,
            UserUpdateInfoRequest userUpdateInfoRequest,
            HttpServletRequest request){
        User loginUser = userApplicationService.getLoginUser(request);
        boolean res = userApplicationService.updateUserAvatar(multipartFile, userUpdateInfoRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 给用户用的更新用户信息
     * @return
     */
    @PostMapping("/update_user_info")
    public BaseResponse<Boolean> updateUserInfo(@RequestBody UserUpdateInfoRequest userUpdateInfoRequest){
        if (userUpdateInfoRequest == null || userUpdateInfoRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 对象转换
        User userEntity = UserAssembler.toUserEntity(userUpdateInfoRequest);
        userApplicationService.updateUser(userEntity);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 对象转换
        User userEntity = UserAssembler.toUserEntity(userUpdateRequest);
        userApplicationService.updateUser(userEntity);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userApplicationService.listUserVOByPage(userQueryRequest));
    }

    @DeductScore(type = UserScoreConstant.MONTH_VIP,
            value = -90L,
            maxCount = -1L)
    @PostMapping("/userSubscribesVip")
    public BaseResponse<Boolean> userSubscribesVip(@RequestBody UserUpdateRequest userUpdateRequest){
        User user = new User();
        user.setId(userUpdateRequest.getId());
        user.setVipType(userUpdateRequest.getVipType());
        User oldUser = userApplicationService.getUserById(userUpdateRequest.getId());
        LocalDate startDate = null;

        Date vipExpiry = oldUser.getVipExpiry();
        if(vipExpiry != null){
            Instant instant = vipExpiry.toInstant();
            ZoneId zone = ZoneId.systemDefault();
            startDate = instant.atZone(zone).toLocalDate();
        }else {
            startDate = LocalDate.now();
        }

        // 获取当前日期后一个月的日期
        LocalDate nextMonth = startDate.plusMonths(1);
        Instant instant = nextMonth.atTime(LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);
        user.setVipExpiry(date);

        userApplicationService.updateUser(user);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(date);

        Message message = new Message();
        message.setUserId(user.getId());
        message.setMessageType(MessageType.SYSTEM.getValue());
        message.setContent(MessageConstant.USER_VIP_MONTH_MESSAGE + formattedDate);
        message.setSenderId(MessageConstant.SYSTEM_SENDER_ID);
        message.setMessageState("0");
        messageApplicationService.messageSend(message);
        return ResultUtils.success(true);
    }
}
