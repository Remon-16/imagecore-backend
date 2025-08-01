package com.tech.imagecorebackendpictureservice.interfaces.controller;



import com.tech.imagecorebackendcommon.common.BaseResponse;
import com.tech.imagecorebackendcommon.common.ResultUtils;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.picture.DoThumbRequest;
import com.tech.imagecorebackendpictureservice.application.service.ThumbApplicationService;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/thumb")
public class ThumbController {

    @Resource
    private UserFeignClient userApplicationService;

    @Resource
    private ThumbApplicationService thumbApplicationService;

    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        Boolean success = thumbApplicationService.doThumb(doThumbRequest, loginUser);
        return ResultUtils.success(success);
    }

    @PostMapping("/undo")
    public BaseResponse<Boolean> undoThumb(@RequestBody DoThumbRequest doThumbRequest, HttpServletRequest request) {
        User loginUser = userApplicationService.getLoginUser(request);
        Boolean success = thumbApplicationService.undoThumb(doThumbRequest, loginUser);
        return ResultUtils.success(success);
    }

}
