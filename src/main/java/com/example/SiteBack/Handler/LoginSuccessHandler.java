package com.example.SiteBack.Handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private String url;
    private Boolean isRedirect;

    public LoginSuccessHandler(String url, Boolean isRedirect){
        this.url = url;
        this.isRedirect = isRedirect;

    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        if (isRedirect) {
            response.sendRedirect(url);
            System.out.println("Redirecting to: " + url);
        } else {
            request.getRequestDispatcher(url).forward(request, response);
            System.out.println("Forwarding to: " + url);
        }
    }
}
