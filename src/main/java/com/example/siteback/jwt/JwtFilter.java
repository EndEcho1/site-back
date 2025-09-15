package com.example.siteback.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Collections;

@Component // 将这个类标记为一个 Spring 组件
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil; // 自定义的 JWT 工具类，用于解析和验证令牌，(Could not autowire. No beans of 'JwtUtil' type found.)将这jwtUtil标记为一个 Spring 组件

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 从请求头中获取 JWT 令牌
        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String token = null;

        // 检查请求头是否包含 Bearer 令牌
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            username = jwtUtil.extractUserId(token); // 从令牌中提取用户名
        }

        // 如果用户名不为空且 SecurityContext 中没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 验证令牌是否有效
            if (jwtUtil.validateToken(token, username,0)) {
                // 将用户名信息添加到 SecurityContext 中
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        // 将请求转发给过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }
}


