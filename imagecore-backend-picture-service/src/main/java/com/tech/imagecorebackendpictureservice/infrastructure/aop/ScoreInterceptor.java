package com.tech.imagecorebackendpictureservice.infrastructure.aop;

import com.tech.imagecorebackendcommon.annotation.AddScore;
import com.tech.imagecorebackendcommon.annotation.DeductScore;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendmodel.dto.user.UserScoreRequest;
import com.tech.imagecorebackendmodel.user.constant.UseScoreConstant;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Aspect
@Component
@Slf4j
@Order(99)
public class ScoreInterceptor {
    @Resource
    private UserFeignClient userFeignClient;

    private User getLoginUser(){
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userFeignClient.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }


    @Around("@annotation(addScore)")
    public Object addScoreInterceptor(ProceedingJoinPoint joinPoint, AddScore addScore) throws Throwable {
        try {
            long score = addScore.value();
            String type = addScore.type();
            long maxCount = addScore.maxCount();

            User loginUser = this.getLoginUser();
            UserScoreRequest userScoreRequest = new UserScoreRequest();
            userScoreRequest.setScoreType(type);
            userScoreRequest.setUserId(loginUser.getId());
            String lock = String.valueOf(loginUser.getId()).intern();
            synchronized (lock) {
                Long curScoreCount = userFeignClient.getUserAddScoreCount(userScoreRequest);
                if(UseScoreConstant.NO_LIMITATION.equals(maxCount) ||curScoreCount < maxCount){
                    userScoreRequest.setScore(score);
                    userFeignClient.userScoreChange(userScoreRequest);
                    log.info("增加积分成功: 用户={}, 积分={}, 类型={}", loginUser.getId(), score, type);
                }
            }
        } catch (Exception e) {
            log.error("积分增加失败", e);
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(deductScore)")
    public Object deductScoreInterceptor(ProceedingJoinPoint joinPoint, DeductScore deductScore) throws Throwable {
        long score = deductScore.value();
        boolean allowNegative = deductScore.allowNegative();
        User loginUser = this.getLoginUser();
        Long userId = loginUser.getId();

        // 会员直接过
        Date vipExpiry = loginUser.getVipExpiry();
        if(vipExpiry != null && vipExpiry.before(new Date())){
            return joinPoint.proceed();
        }

        long maxCount = deductScore.maxCount();
        String type = deductScore.type();
        UserScoreRequest userScoreRequest = new UserScoreRequest();
        userScoreRequest.setScoreType(type);
        userScoreRequest.setUserId(loginUser.getId());
        Long curScoreCount = userFeignClient.getUserAddScoreCount(userScoreRequest);
        if(!(curScoreCount < maxCount) && !UseScoreConstant.NO_LIMITATION.equals(maxCount)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "今日次数已到上限");
        }

        String lock = String.valueOf(loginUser.getId()).intern();
        synchronized (lock) {
            // 检查积分是否足够
            if (!userFeignClient.checkScore(userScoreRequest)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分不足");
            }

            // 执行原方法
            Object result = joinPoint.proceed();
            userScoreRequest.setScore(score);
            // 扣除积分
            userFeignClient.userScoreChange(userScoreRequest);
            log.info("扣除积分成功: 用户={}, 积分={}", userId, score);
            return result;
        }
    }
}
