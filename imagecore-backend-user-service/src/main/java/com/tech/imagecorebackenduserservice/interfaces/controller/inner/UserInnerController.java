package com.tech.imagecorebackenduserservice.interfaces.controller.inner;

import com.tech.imagecorebackendmodel.dto.user.UserScoreRequest;
import com.tech.imagecorebackendmodel.user.entity.ScoreUser;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
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
    ScoreUserApplicationService scoreUserApplicationService;

    @Override
    @GetMapping("/get/id")
    public User getUserById(@RequestParam("userId") long userId) {
        return userApplicationService.getUserById(userId);
    }

    @Override
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam("idList") Collection<Long> idList) {
        return userApplicationService.listByIds((Set<Long>) idList);
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
    public Boolean checkScore(@RequestBody UserScoreRequest userScoreRequest) {
        return userApplicationService.checkScore(userScoreRequest.getUserId(), userScoreRequest.getScore());
    }

    @Override
    @PostMapping("/score/batchUpdateScore")
    public void batchUpdateScore(@RequestBody Map<Long, Long> scoreMap) {
        userApplicationService.batchUpdateScore(scoreMap);
    }

    @Override
    @PostMapping("/scoreUser/saveBatch")
    public void saveBatch(List<ScoreUser> scoreUserList) {
        scoreUserApplicationService.saveBatch(scoreUserList);
    }
}
