package com.tech.imagecorebackenduserservice.domain.user.service;

import com.tech.imagecorebackendmodel.user.entity.ScoreUser;

import java.util.List;

public interface ScoreUserDomainService {
    void saveBatch(List<ScoreUser> scoreUserList);
}
