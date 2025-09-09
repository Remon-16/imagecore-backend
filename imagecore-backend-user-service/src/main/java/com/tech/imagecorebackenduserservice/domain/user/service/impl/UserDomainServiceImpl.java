package com.tech.imagecorebackenduserservice.domain.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.exception.ThrowUtils;
import com.tech.imagecorebackendcommon.utils.CacheUtils;
import com.tech.imagecorebackendcommon.utils.JwtUtils;
import com.tech.imagecorebackendmodel.dto.user.UserChangeScoreRequest;
import com.tech.imagecorebackendmodel.dto.user.UserQueryRequest;
import com.tech.imagecorebackendmodel.dto.user.UserUpdateInfoRequest;
import com.tech.imagecorebackendmodel.user.constant.UserConstant;
import com.tech.imagecorebackendmodel.user.constant.UserScoreConstant;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.user.valueobject.UserRedisLuaScriptConstant;
import com.tech.imagecorebackendmodel.user.valueobject.UserRoleEnum;
import com.tech.imagecorebackendmodel.user.valueobject.UserScoreEnum;
import com.tech.imagecorebackendmodel.user.valueobject.UserVipEnum;
import com.tech.imagecorebackendmodel.vo.user.LoginUserVO;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import com.tech.imagecorebackendserviceclient.application.service.PictureFeignClient;
import com.tech.imagecorebackenduserservice.domain.user.repository.UserRepository;
import com.tech.imagecorebackenduserservice.domain.user.service.UserDomainService;
import com.tech.imagecorebackenduserservice.infrastructure.dco.UserCacheHandler;
import com.tech.imagecorebackenduserservice.infrastructure.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import jakarta.annotation.Resource;
import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Remon
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-12-09 20:03:03
 */
@Service
@Slf4j
public class UserDomainServiceImpl implements UserDomainService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    UserCacheHandler userCacheHandler;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PictureFeignClient pictureFeignClient;

    private String getTimeSlice() {
        DateTime nowDate = DateUtil.date();
        // 获取到当前时间前最近的整数秒，比如当前 11:20:23 ，获取到 11:20:20
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 2. 检查用户账号是否和数据库中已有的重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userRepository.getBaseMapper().selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 密码一定要加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = userRepository.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 2. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询数据库中的用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userRepository.getBaseMapper().selectOne(queryWrapper);
        // 不存在，抛异常
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        // 4. 保存用户的登录态
        SecretKey secretKey = JwtUtils.createSecretKey();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userAccount", userAccount);
        claims.put("userId", user.getId());
        claims.put("userRole", user.getUserRole());

        String token = JwtUtils.generateToken(claims, user.getUserAccount(), secretKey);
        request.setAttribute(JwtUtils.JWT_HEADER, token);

        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "imgsalt";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public User getUserFromRequest(HttpServletRequest request){

        String userIdStr = request.getHeader("userId");
        String userAccount = request.getHeader("userAccount");
        String userRole = request.getHeader("userRole");
        if (userIdStr == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        try {
            // 构建用户对象
            User currentUser = new User();
            currentUser.setId(Long.parseLong(userIdStr));
            currentUser.setUserAccount(userAccount);
            currentUser.setUserRole(userRole);
            return currentUser;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息格式错误");
        }
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User currentUser = getUserFromRequest(request);
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 数据库查完整信息
        currentUser = this.getById(currentUser.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取脱敏类的用户信息
     *
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获得脱敏后的用户信息
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户列表
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 前端删除token即可，这里保留一个拓展。
        return true;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public Boolean removeById(Long id) {
        return userRepository.removeById(id);
    }

    @Override
    public boolean updateById(User user) {
        return userRepository.updateById(user);
    }

    @Override
    public User getById(long id) {
        return userRepository.getById(id);
    }

    @Override
    public Page<User> page(Page<User> userPage, QueryWrapper<User> queryWrapper) {
        return userRepository.page(userPage, queryWrapper);
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return userRepository.listByIds(userIdSet);
    }

    @Override
    public boolean saveUser(User userEntity) {
        return userRepository.save(userEntity);
    }

    @Override
    public void userScoreChange(Long userId, String scoreType, Long score) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户为空");
        ThrowUtils.throwIf(scoreType == null, ErrorCode.PARAMS_ERROR, "变动原因为空");
        ThrowUtils.throwIf(score == null, ErrorCode.PARAMS_ERROR, "积分为空");
        String timeSlice = getTimeSlice();
        String userTempScoreKey = UserCacheHandler.getRedisKey(CacheUtils.getUserTempScoreCacheKey(timeSlice));
        String userScoreKey = UserCacheHandler.getRedisKey(CacheUtils.getUserScoreCacheKey(userId.toString()));

        String userScoreType = UserCacheHandler.getRedisKey(CacheUtils.getUserScoreCountKey(scoreType, userId.toString()));

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.SCORE_HANDLE_SCRIPT,
                Arrays.asList(userTempScoreKey, userScoreKey, userScoreType),
                userId.toString(),
                score,
                scoreType
        );

    }

    @Override
    public Long getUserAddScoreCount(Long userId, String scoreType) {
        String scoreAddCountKey = CacheUtils.getUserScoreCountKey(scoreType, userId.toString());
        return userCacheHandler.getUserAddScoreCount(scoreAddCountKey);
    }

    @Override
    public Boolean checkScore(Long userId, Long score) {
        // 查缓存
        String userScoreKey = CacheUtils.getUserScoreCacheKey(userId.toString());
        Long userScore = userCacheHandler.getUserCore(userScoreKey);
        // 缓存没有，查数据库
        if (userScore == null) {
            userScore = userRepository.getById(userId).getUserScore();
            userCacheHandler.setUserCore(userScoreKey, userScore);
        }
        return userScore >= Math.abs(score);
    }

    @Override
    public void batchUpdateScore(Map<Long, Long> scoreMap) {
        userMapper.batchUpdateScore(scoreMap);
    }

    @Override
    public void userSubscribesVip(User user) {
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户为空");
        UserVipEnum.assertEnumValue(user.getVipType());
        String vipDateStr = UserVipEnum.getEnumByText(user.getVipType()).getValue();
        // 将字符串转换为整数
        int days = Integer.parseInt(vipDateStr);
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        // 添加指定天数
        calendar.add(Calendar.DAY_OF_YEAR, days);
        // 返回新的Date对象
        Date vipDate = calendar.getTime();
        // 更新会员
        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setVipType(user.getVipType());
        newUser.setVipExpiry(vipDate);
        userRepository.updateById(newUser);
    }

    @Override
    public boolean updateUserAvatar(MultipartFile avatar, UserUpdateInfoRequest userUpdateInfoRequest, User loginUser) {
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        String url = pictureFeignClient.uploadUserAvatar(avatar, uploadPathPrefix);
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAvatar(url);
        userRepository.updateById(user);
        return true;
    }

    @Override
    public void userAddScore(UserChangeScoreRequest userChangeScoreRequest) {
        try {
            UserScoreEnum userScoreEnum = UserScoreEnum.getEnumByScoreType(userChangeScoreRequest.getScoreType());
            long score = userScoreEnum.getScore();
            long maxCount = userScoreEnum.getMaxCount();
            String type = userChangeScoreRequest.getScoreType();
            String scoreAddCountKey = CacheUtils.getUserScoreCountKey(type, userChangeScoreRequest.getUserId().toString());
            String lock = String.valueOf(userChangeScoreRequest.getUserId()).intern();
            synchronized (lock) {
                Long curScoreCount = userCacheHandler.getUserAddScoreCount(scoreAddCountKey);
                if(UserScoreConstant.NO_LIMITATION.equals(maxCount) ||curScoreCount < maxCount){
                    this.userScoreChange(userChangeScoreRequest.getUserId(), type, score);
                    log.info("增加积分成功: 用户={}, 积分={}, 类型={}", userChangeScoreRequest.getUserId(), score, type);
                }
            }
        } catch (Exception e) {
            log.error("积分增加失败", e);
        }
    }
}




