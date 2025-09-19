package com.tech.imagecorebackenduserservice.interfaces.controller.inner;

import cn.hutool.json.JSONUtil;
import com.tech.imagecorebackendmodel.dto.user.UserChangeScoreRequest;
import com.tech.imagecorebackendmodel.dto.user.UserScoreRequest;
import com.tech.imagecorebackendmodel.user.constant.UserScoreConstant;
import com.tech.imagecorebackendmodel.user.entity.Message;
import com.tech.imagecorebackendmodel.user.entity.ScoreUser;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.user.UserListVO;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import com.tech.imagecorebackenduserservice.application.service.MessageApplicationService;
import com.tech.imagecorebackenduserservice.application.service.ScoreUserApplicationService;
import com.tech.imagecorebackenduserservice.application.service.UserApplicationService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private ScoreUserApplicationService scoreUserApplicationService;

    @Resource
    private MessageApplicationService messageApplicationService;

    @Override
    @GetMapping("/get/id")
    public User getUserById(@RequestParam("userId") long userId) {
        return userApplicationService.getUserById(userId);
    }

    @Override
    @PostMapping("/post/ids")
    public UserListVO listByIds(@RequestBody Set<Long> idList) {
        UserListVO userListVO = new UserListVO();
        List<User> userList = userApplicationService.listByIds(idList);
        userListVO.setUserListJson(JSONUtil.toJsonStr(userList));
        return userListVO;
    }

    @Override
    @PostMapping("/score/change")
    public void userScoreChange(@RequestBody UserScoreRequest userScoreRequest) {
        userApplicationService.userScoreChange(userScoreRequest.getUserId(), userScoreRequest.getScoreType(), userScoreRequest.getScore());
    }

    @Override
    @PostMapping("/score/useCount")
    public Long getUserAddScoreCount(@RequestBody UserScoreRequest userScoreRequest) {
        return userApplicationService.getUserAddScoreCount(userScoreRequest.getUserId(), userScoreRequest.getScoreType());
    }

    @Override
    @PostMapping("/score/checkScore")
    public String checkScore(@RequestBody UserScoreRequest userScoreRequest) {
        boolean f = userApplicationService.checkScore(userScoreRequest.getUserId(), userScoreRequest.getScore());
        return f? UserScoreConstant.YES : UserScoreConstant.NO;
    }

    @Override
    @PostMapping("/score/batchUpdateScore")
    public void batchUpdateScore(@RequestBody Map<Long, Long> scoreMap) {
        userApplicationService.batchUpdateScore(scoreMap);
    }

    @Override
    @PostMapping("/score/userAddScore")
    public void userAddScore(@RequestBody UserChangeScoreRequest userChangeScoreRequest) {
        userApplicationService.userAddScore(userChangeScoreRequest);
    }

    @Override
    @PostMapping("/scoreUser/saveBatch")
    public void saveBatch(@RequestBody List<ScoreUser> scoreUserList) {
        scoreUserApplicationService.saveBatch(scoreUserList);
    }

    @Override
    @PostMapping("/message/messageSend")
    public void messageSend(@RequestBody Message message) {
        messageApplicationService.messageSend(message);
    }

    @Override
    @PostMapping("/message/messageBatchSend")
    public void messageBatchSend(@RequestBody List<Message> messageList) {
        messageApplicationService.messageBatchSend(messageList);
    }
}
