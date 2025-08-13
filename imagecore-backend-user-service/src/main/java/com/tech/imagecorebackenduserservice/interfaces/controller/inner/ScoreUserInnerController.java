package com.tech.imagecorebackenduserservice.interfaces.controller.inner;

import com.tech.imagecorebackendmodel.user.entity.ScoreUser;
import com.tech.imagecorebackendserviceclient.application.service.ScoreUserFeignClient;
import com.tech.imagecorebackenduserservice.application.service.ScoreUserApplicationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inner")
public class ScoreUserInnerController implements ScoreUserFeignClient {

    @Resource
    ScoreUserApplicationService scoreUserApplicationService;

    @Override
    @PostMapping("/scoreUser/saveBatch")
    public void saveBatch(List<ScoreUser> scoreUserList) {
        scoreUserApplicationService.saveBatch(scoreUserList);
    }
}
