package com.tech.imagecorebackendspaceservice.application.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendmodel.dto.space.SpaceAddRequest;
import com.tech.imagecorebackendmodel.dto.space.SpaceQueryRequest;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceIncreaseUsageRequest;
import com.tech.imagecorebackendmodel.dto.space.inner.SpaceReduceUsageRequest;
import com.tech.imagecorebackendmodel.space.entity.Space;
import com.tech.imagecorebackendmodel.space.entity.SpaceUser;
import com.tech.imagecorebackendmodel.space.valueobject.SpaceLevelEnum;
import com.tech.imagecorebackendmodel.space.valueobject.SpaceRoleEnum;
import com.tech.imagecorebackendmodel.space.valueobject.SpaceTypeEnum;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.space.SpaceVO;
import com.tech.imagecorebackendmodel.vo.user.UserListVO;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import com.tech.imagecorebackendspaceservice.application.service.SpaceApplicationService;
import com.tech.imagecorebackendspaceservice.application.service.SpaceUserApplicationService;
import com.tech.imagecorebackendspaceservice.domain.space.service.SpaceDomainService;
import com.tech.imagecorebackendspaceservice.infrastructure.mapper.SpaceMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Remon
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2024-12-18 19:53:34
 */
@Service
public class SpaceApplicationServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceApplicationService {

    @Resource
    private SpaceDomainService spaceDomainService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private SpaceUserApplicationService spaceUserApplicationService;

    @Resource
    private TransactionTemplate transactionTemplate;
    @Qualifier("spaceDomainService")

    // 为了方便部署，注释掉分表
//    @Resource
//    @Lazy
//    private DynamicShardingManager dynamicShardingManager;

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1. 填充参数默认值
        // 转换实体类和 DTO
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 填充容量和大小
        this.fillSpaceBySpaceLevel(space);
        // 2. 校验参数
        space.validSpace(true);
        // 3. 校验权限，非管理员只能创建普通级别的空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !loginUser.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        // 4. 控制同一用户只能创建一个私有空间、以及一个团队空间
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 判断是否已有空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType, space.getSpaceType())
                        .exists();
                // 如果已有空间，就不能再创建
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户每类空间只能创建一个");
                // 创建
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存空间到数据库失败");
                // 创建成功后，如果是团队空间，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    result = spaceUserApplicationService.save(spaceUser);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                }
//                // 创建分表（仅对团队空间生效）为方便部署，暂时不使用
//                dynamicShardingManager.createSpacePictureTable(space);
                // 返回新写入的数据 id
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userFeignClient.getUserById(userId);
            UserVO userVO = userFeignClient.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        UserListVO userListVO = userFeignClient.listByIds(userIdSet);
        List<User> userList = userListVO.getUserList(userListVO.getUserListJson());
        Map<Long, List<User>> userIdUserListMap = userList.stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userFeignClient.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        return spaceDomainService.getQueryWrapper(spaceQueryRequest);
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        spaceDomainService.fillSpaceBySpaceLevel(space);
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        spaceDomainService.checkSpaceAuth(loginUser, space);
    }

    @Override
    public boolean increaseUsage(SpaceIncreaseUsageRequest spaceIncreaseUsageRequest) {
        return this.lambdaUpdate()
                .eq(Space::getId, spaceIncreaseUsageRequest.getSpaceId())
                .setSql("totalSize = totalSize + " + spaceIncreaseUsageRequest.getPicSize())
                .setSql("totalCount = totalCount + 1")
                .update();
    }

    @Override
    public boolean reduceUsage(SpaceReduceUsageRequest spaceReduceUsageRequest) {
        return this.lambdaUpdate()
                .eq(Space::getId, spaceReduceUsageRequest.getSpaceId())
                .setSql("totalSize = totalSize - " + spaceReduceUsageRequest.getPicSize())
                .setSql("totalCount = totalCount - 1")
                .update();
    }
}


