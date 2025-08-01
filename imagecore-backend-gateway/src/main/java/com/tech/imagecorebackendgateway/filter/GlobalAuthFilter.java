package com.tech.imagecorebackendgateway.filter;

import com.tech.imagecorebackendcommon.exception.ErrorCode;
import com.tech.imagecorebackendcommon.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.Resource;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Resource
    private SecretKey secretKey;
    // 不需要验证token的路径
    private static final String[] WHITE_LIST = {
            "/api/user/login",
            "/api/user/get/login",
            "/api/user/register",
            "/api/user/logout",
            "/api/doc.html",
            "/api/v3/api-docs",
            "/api/v2/api-docs",
            "/api/swagger-resources",
            "/api/swagger-ui.html",
            "/api/webjars/**"
    };
    /**
     * 判断是否为白名单路径
     * @param path 请求路径
     * @return 是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        for (String whitePath : WHITE_LIST) {
            if (antPathMatcher.match(whitePath, path)) {
                return true;
            }
        }
        return false;
    }
    /**
     * 处理无权限的情况
     * 当用户请求需要权限但未提供有效凭证时调用此方法
     * @param response 响应对象，用于向客户端发送响应
     * @param code 错误码，表示具体的错误类型
     * @param message 错误信息，描述错误的详细信息
     * @return Mono<Void> 表示异步操作的完成
     */
    private Mono<Void> handleNoAuth(ServerHttpResponse response, int code, String message) {
        // 设置响应状态码为未授权
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // 设置响应头，指定内容类型和字符集
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 创建DataBufferFactory，用于生成数据缓冲区
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        // 构建JSON响应体，包含错误码和错误信息
        String json = String.format("{\"code\":%d,\"data\":null,\"message\":\"%s\"}", code, message);
        // 将JSON响应体包装到数据缓冲区中
        DataBuffer dataBuffer = dataBufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));
        // 使用Mono将数据缓冲区写入响应中
        return response.writeWith(Mono.just(dataBuffer));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        String path = serverHttpRequest.getURI().getPath();
        // 判断路径中是否包含 inner，只允许内部调用
        if (antPathMatcher.match("/ **/inner/** ", path)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            DataBuffer dataBuffer = dataBufferFactory.wrap("无权限".getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(dataBuffer));
        }

        // 白名单路径直接放行
        if (isWhiteListPath(path)) {
            return chain.filter(exchange);
        }

        // 1. 如果是OPTIONS预检请求，直接放行
        if (HttpMethod.OPTIONS.matches(Objects.requireNonNull(serverHttpRequest.getMethod()).name())) {
            return chain.filter(exchange);
        }
        // 统一权限校验，通过 JWT 获取登录用户信息
        String token = serverHttpRequest.getHeaders().getFirst(JwtUtils.JWT_HEADER);

        // 如果请求头中没有token
        if (StringUtils.isBlank(token)) {
            return handleNoAuth(exchange.getResponse(), ErrorCode.NOT_LOGIN_ERROR.getCode(), ErrorCode.NOT_LOGIN_ERROR.getMessage());
        }

        // 如果token以Bearer 开头，则去掉前缀
        if (token.startsWith(JwtUtils.JWT_TOKEN_PREFIX)) {
            token = token.substring(JwtUtils.JWT_TOKEN_PREFIX.length());
        }

        // 验证token
        if (!JwtUtils.validateToken(token, secretKey)) {
            return handleNoAuth(exchange.getResponse(), ErrorCode.NOT_LOGIN_ERROR.getCode(), "Token无效或已过期");
        }
        try {
            // 从token中获取用户信息
            Long userId = JwtUtils.getUserIdFromToken(token, secretKey);
            String userAccount = JwtUtils.getUserAccountFromToken(token, secretKey);
            String userRole = JwtUtils.getUserRoleFromToken(token, secretKey);
            // 将用户信息添加到请求头中，传递给下游服务
            ServerHttpRequest mutableRequest = serverHttpRequest.mutate()
                    .header("userId", String.valueOf(userId))
                    .header("userAccount", userAccount)
                    .header("userRole", userRole)
                    .build();

            // 使用修改后的请求继续过滤器链
            return chain.filter(exchange.mutate().request(mutableRequest).build());
        } catch (Exception e) {
            return handleNoAuth(exchange.getResponse(), ErrorCode.SYSTEM_ERROR.getCode(), "Token解析失败");
        }
    }

    /**
     * 优先级提到最高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
