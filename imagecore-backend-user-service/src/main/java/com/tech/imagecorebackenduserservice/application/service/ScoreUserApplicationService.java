package com.tech.imagecorebackenduserservice.application.service;

import com.tech.imagecorebackendmodel.user.entity.ScoreUser;

import java.util.List;

public interface ScoreUserApplicationService {

    void saveBatch(List<ScoreUser> scoreUserList);
}
