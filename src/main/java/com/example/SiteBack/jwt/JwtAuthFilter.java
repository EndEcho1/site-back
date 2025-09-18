package com.example.SiteBack.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 过滤器：从请求中提取并校验 JWT token，设置用户上下文。
 * 暂时不添加组件注解，不使用自动注入
 */
//@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");


        // 1. 跳过无需认证的路径（如登录、公开API）
        if (shouldSkipAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Token 提取与基础验证
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, "缺少有效的 Authorization 头", HttpStatus.UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);
        String userId;
        Integer level;

        try {
            // 3. 解析 Token
            userId = jwtUtil.extractUserId(token);  // 注意方法名统一性（extractUserid → extractUserId）
            level = jwtUtil.extractLevel(token);

            // 4. 验证 Token 有效性
            if (!jwtUtil.validateToken(token, userId, level)) {
                sendError(response, "Token 无效", HttpStatus.UNAUTHORIZED);
                return;
            }

            // 5. 构建认证对象
            Authentication authentication = buildAuthentication(token, userId, level, request);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            sendError(response, "Token 已过期", HttpStatus.UNAUTHORIZED);
            return;
        } catch (JwtException | IllegalArgumentException e) {
            sendError(response, "Token 解析失败: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            return;
        }

        // 6. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    // --- 辅助方法 ---
    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth") || path.equals("/error");
    }

    private Authentication buildAuthentication(String token, String userId, Integer level,
                                               HttpServletRequest request) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (level != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_LVL_" + level));
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }

    private void sendError(HttpServletResponse response, String message, HttpStatus status) {
        response.setStatus(status.value());
        response.setContentType("application/json");
        try {
            response.getWriter().write("{\"error\": \"" + message + "\"}");
        } catch (IOException ignored) {
        }
    }
}