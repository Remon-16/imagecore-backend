package com.tech.imagecorebackendspaceservice.interfaces.controller.inner;

import com.tech.imagecorebackendmodel.dto.space.inner.PermissionListRequest;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceIncreaseUsageRequest;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceReduceUsageRequest;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceUserAuthRequest;
import com.tech.imagecorebackendmodel.space.entity.Space;
import com.tech.imagecorebackendserviceclient.application.service.SpaceFeignClient;
import com.tech.imagecorebackendspaceservice.application.service.SpaceApplicationService;
import com.tech.imagecorebackendspaceservice.auth.SpaceUserAuthManager;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/inner")
public class SpaceInnerController implements SpaceFeignClient {

    @Resource
    private SpaceApplicationService spaceApplicationService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Override
    @GetMapping("/get/id")
    public Space getById(@RequestParam("spaceId") Long spaceId) {
        return spaceApplicationService.getById(spaceId);
    }

    @Override
    @PostMapping("/space_submit/increase")
    public boolean increaseUsage(@RequestBody SpaceIncreaseUsageRequest spaceIncreaseUsageRequest) {
        return spaceApplicationService.increaseUsage(spaceIncreaseUsageRequest);
    }

    @Override
    @PostMapping("/space_submit/reduce")
    public boolean reduceUsage(@RequestBody SpaceReduceUsageRequest spaceReduceUsageRequest) {
        return spaceApplicationService.reduceUsage(spaceReduceUsageRequest);
    }

    @Override
    @PostMapping("/auth/has_permission")
    public Boolean hasPermission(@RequestBody SpaceUserAuthRequest spaceUserAuthRequest) {
        return spaceUserAuthManager.hasPermission(
                spaceUserAuthRequest.getSpace(),
                spaceUserAuthRequest.getLoginUser(),
                spaceUserAuthRequest.getNeedPermission()
        );
    }

    @Override
    public List<String> getPermissionList(PermissionListRequest permissionListRequest) {
        return spaceUserAuthManager.getPermissionList(
                permissionListRequest.getSpace(),
                permissionListRequest.getLoginUser()
        );
    }
}
