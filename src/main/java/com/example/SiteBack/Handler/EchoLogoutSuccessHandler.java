package com.example.SiteBack.Handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.io.IOException;

public class EchoLogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess (HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        //销毁会话，清空缓存
        request.getSession().invalidate();
        authentication.setAuthenticated(false);//设置未登录状态
        response.sendRedirect("/login");//重定向
    }
}