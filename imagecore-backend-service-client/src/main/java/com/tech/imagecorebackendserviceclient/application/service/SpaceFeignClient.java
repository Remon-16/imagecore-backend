package com.tech.imagecorebackendserviceclient.application.service;


import com.tech.imagecorebackendmodel.dto.space.inner.PermissionListRequest;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceIncreaseUsageRequest;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceReduceUsageRequest;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceUserAuthRequest;
import com.tech.imagecorebackendmodel.space.entity.Space;
import com.tech.imagecorebackendmodel.user.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * @author Remon
 */
@FeignClient(name = "imagecore-backend-space-service", path = "/api/space/inner")
public interface SpaceFeignClient {

    @GetMapping("/get/id")
    Space getById(@RequestParam("spaceId") Long spaceId);

    @PostMapping("/space_submit/increase")
    boolean increaseUsage(@RequestBody SpaceIncreaseUsageRequest spaceIncreaseUsageRequest);

    @PostMapping("/space_submit/reduce")
    boolean reduceUsage(@RequestBody SpaceReduceUsageRequest spaceReduceUsageRequest);

    @PostMapping("/auth/has_permission")
    Boolean hasPermission(@RequestBody SpaceUserAuthRequest spaceUserAuthRequest);

    @PostMapping("/auth/get_permission_lisy")
    List<String> getPermissionList(@RequestBody PermissionListRequest permissionListRequest);

}
