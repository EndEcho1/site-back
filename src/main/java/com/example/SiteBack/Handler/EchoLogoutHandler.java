package com.example.SiteBack.Handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public class EchoLogoutHandler implements LogoutHandler {
    //退出时处理逻辑
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        //额外处理，保存额外的attribute到数据库
        //日志数据等
        System.out.println("ex data");

    }
}