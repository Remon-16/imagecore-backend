package com.tech.imagecorebackendserviceclient.application.service;



import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendmodel.dto.user.UserChangeScoreRequest;
import com.tech.imagecorebackendmodel.dto.user.UserScoreRequest;
import com.tech.imagecorebackendmodel.user.entity.ScoreUser;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendmodel.vo.user.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Remon
 */
@FeignClient(name = "imagecore-backend-user-service", path = "/api/user/inner")
public interface UserFeignClient {

    /**
     * 根据 id 获取用户
     * @param userId
     * @return
     */
    @GetMapping("/get/id")
    User getUserById(@RequestParam("userId") long userId);

    /**
     * 根据 id 获取用户列表
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    List<User> listByIds(@RequestParam("idList") Collection<Long> idList);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    default User getLoginUser(HttpServletRequest request){
        // 构建用户对象
        User currentUser = getUserFromRequest(request);
        if (currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    default UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }



    default User getUserFromRequest(HttpServletRequest request){
        String userIdStr = request.getHeader("userId");
        String userAccount = request.getHeader("userAccount");
        String userRole = request.getHeader("userRole");

        if (StringUtils.isEmpty(userIdStr)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 构建用户对象
        User currentUser = new User();
        try {
            currentUser.setId(Long.parseLong(userIdStr));
            currentUser.setUserAccount(userAccount);
            currentUser.setUserRole(userRole);
            return currentUser;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息格式错误");
        }
    }

    @PostMapping("/score/change")
    void userScoreChange(@RequestBody UserScoreRequest userScoreRequest);

    @PostMapping("/score/useCount")
    Long getUserAddScoreCount(@RequestBody UserScoreRequest userScoreRequest);

    @PostMapping("/score/checkScore")
    Boolean checkScore(@RequestBody UserScoreRequest userScoreRequest);

    @PostMapping("/score/batchUpdateScore")
    void batchUpdateScore(@RequestBody Map<Long, Long> scoreMap);

    @PostMapping("/score/userAddScore")
    void userAddScore(UserChangeScoreRequest userChangeScoreRequest);

    @PostMapping("/scoreUser/saveBatch")
    void saveBatch(List<ScoreUser> scoreUserList);

}
