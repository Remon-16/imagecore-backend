package com.tech.imagecorebackendserviceclient.application.service;

import com.tech.imagecorebackendmodel.user.entity.ScoreUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "imagecore-backend-user-service", path = "/api/user/inner")
public interface ScoreUserFeignClient {
    @PostMapping("/scoreUser/saveBatch")
    void saveBatch(List<ScoreUser> scoreUserList);
}
