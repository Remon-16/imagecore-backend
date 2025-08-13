package com.tech.imagecorebackenduserservice.application.service.impl;

import com.tech.imagecorebackendmodel.user.entity.ScoreUser;
import com.tech.imagecorebackenduserservice.application.service.ScoreUserApplicationService;
import com.tech.imagecorebackenduserservice.domain.user.service.ScoreUserDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreUserApplicationServiceImpl implements ScoreUserApplicationService {
    @Resource
    ScoreUserDomainService scoreUserDomainService;

    @Override
    public void saveBatch(List<ScoreUser> scoreUserList) {
        scoreUserDomainService.saveBatch(scoreUserList);
    }
}
