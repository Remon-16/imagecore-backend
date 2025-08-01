package com.tech.imagecorebackendpictureservice.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.tech.imagecorebackendcommon.exception.BusinessException;
import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.utils.JwtUtils;
import com.tech.imagecorebackendmodel.auth.SpaceUserPermissionConstant;
import com.tech.imagecorebackendmodel.dto.space.inner.PermissionListRequest;
import com.tech.imagecorebackendmodel.picture.entity.Picture;
import com.tech.imagecorebackendmodel.space.entity.Space;
import com.tech.imagecorebackendmodel.space.valueobject.SpaceTypeEnum;
import com.tech.imagecorebackendmodel.user.entity.User;
import com.tech.imagecorebackendserviceclient.application.service.PictureFeignClient;
import com.tech.imagecorebackendserviceclient.application.service.SpaceFeignClient;
import com.tech.imagecorebackendserviceclient.application.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.annotation.Resource;
import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 拦截器，建立连接前要先校验
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    private static final String TOKEN_PREFIX = "Bearer_";

    @Resource
    private SecretKey secretKey;
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private PictureFeignClient pictureFeignClient;

    @Resource
    private SpaceFeignClient spaceFeignClient;

    /**
     * 建立连接前要先校验
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes 给 WebSocketSession 会话设置属性
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        User loginUser = new User();
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();

            // 获取子协议列表
            List<String> protocols = request.getHeaders().get("Sec-WebSocket-Protocol");

            if (protocols != null && !protocols.isEmpty()) {
                for (String protocol : protocols) {
                    // 查找包含 Bearer token 的子协议
                    if (protocol.startsWith(TOKEN_PREFIX)) {
                        String token = protocol.substring(TOKEN_PREFIX.length());

                        // 验证 token
                        if (JwtUtils.validateToken(token, secretKey)) {
                            // 将用户信息存入属性
                            // 从token中获取用户信息
                            Long userId = JwtUtils.getUserIdFromToken(token, secretKey);
                            String userAccount = JwtUtils.getUserAccountFromToken(token, secretKey);
                            String userRole = JwtUtils.getUserRoleFromToken(token, secretKey);
                            // 构建用户对象并存入请求属性中，供后续使用
                            loginUser.setId(userId);
                            loginUser.setUserAccount(userAccount);
                            loginUser.setUserRole(userRole);
                            response.getHeaders().set("Sec-WebSocket-Protocol", protocol);
                            break;
                        }else {
                            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "token无效或已过期");
                        }
                    }
                }
            }

            // 从请求中获取参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("缺少图片参数，拒绝握手");
                return false;
            }
            // 获取当前登录用户
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("用户未登录，拒绝握手");
                return false;
            }
            // 校验用户是否有编辑当前图片的权限
            Picture picture = pictureFeignClient.getById(Long.valueOf(pictureId));
            if (ObjUtil.isEmpty(picture)) {
                log.error("图片不存在，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            if (spaceId != null) {
                space = spaceFeignClient.getById(spaceId);
                if (ObjUtil.isEmpty(space)) {
                    log.error("图片所在空间不存在，拒绝握手");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.error("图片所在空间不是团队空间，拒绝握手");
                    return false;
                }
            }
            PermissionListRequest permissionListRequest = new PermissionListRequest();
            permissionListRequest.setLoginUser(loginUser);
            permissionListRequest.setSpace(space);
            List<String> permissionList = spaceFeignClient.getPermissionList(permissionListRequest);
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                log.error("用户没有编辑图片的权限，拒绝握手");
                return false;
            }
            // 设置用户登录信息等属性到 WebSocket 会话中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId)); // 记得转换为 Long 类型
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
