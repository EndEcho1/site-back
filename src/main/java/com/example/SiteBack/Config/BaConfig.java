package com.example.SiteBack.Config;


import com.example.SiteBack.Controller.HttpEum.ECode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.validation.constraints.NotNull;
import module jakarta.validation;


@Configuration // 表示这是一个配置类，它会被 Spring 框架作为配置类来处理
public class BaConfig implements WebMvcConfigurer {

    @Bean // 将方法的返回值作为一个 Bean 注入到 Spring 的应用上下文中
    public WebMvcConfigurer corsConfigurer() {
        // 返回一个匿名的 WebMvcConfigurer 实现，用于配置 CORS
        return new WebMvcConfigurer() {

            // 重写 addCorsMappings 方法来配置 CORS 映射
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                // 配置允许跨域访问的路径模式
                registry.addMapping("/**")
                        // 允许的来源（Origin），这里指定了 http://localhost:8080
                        .allowedOrigins("*") // 改为前端地址
                        // 允许的 HTTP 方法（GET、POST、PUT、DELETE、OPTIONS）
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // 允许的请求头
                        .allowedHeaders("*")
                        //允许 私有网络 中的请求。
                        .allowPrivateNetwork(true)
                        // 允许浏览器发送的自定义请求头
                        .exposedHeaders(ECode.E_CODE.getHeaderE_NAME(), ECode.E_CODE_MSG.getHeaderE_NAME())
                        // 预检请求的有效期，单位为秒
                        .maxAge(3600)
                        // 是否允许携带凭证（如 Cookie）
                        .allowCredentials(true);
            }
        };
    }
}

