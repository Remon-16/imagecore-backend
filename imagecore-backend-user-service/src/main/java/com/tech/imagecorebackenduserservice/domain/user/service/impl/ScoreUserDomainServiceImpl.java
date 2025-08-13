package com.tech.imagecorebackenduserservice.domain.user.service.impl;

import com.tech.imagecorebackendmodel.user.entity.ScoreUser;
import com.tech.imagecorebackenduserservice.domain.user.repository.ScoreUserRepository;
import com.tech.imagecorebackenduserservice.domain.user.service.ScoreUserDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreUserDomainServiceImpl implements ScoreUserDomainService {

    @Resource
    private ScoreUserRepository scoreUserRepository;
    @Override
    public void saveBatch(List<ScoreUser> scoreUserList) {
        scoreUserRepository.saveBatch(scoreUserList);
    }
}
