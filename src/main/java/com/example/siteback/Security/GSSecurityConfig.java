package com.example.siteback.Security;

import com.example.siteback.Handler.EchoLogoutHandler;
import com.example.siteback.Handler.EchoLogoutSuccessHandler;
import com.example.siteback.Handler.LoginFailHandler;
import com.example.siteback.Handler.LoginSuccessHandler;
import com.example.siteback.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class GSSecurityConfig  {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    public GSSecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;

    }
        private final JwtAuthFilter jwtAuthFilter;
    */





    /**
     * 配置SecurityFilterChain，定义应用的安全策略。
     *
     * @param echo HttpSecurity对象，用于配置HTTP安全性
     * @return SecurityFilterChain 对象
     * @throws Exception 异常
     */
/**
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity echo) throws Exception{
        echo.formLogin(formLogin -> formLogin
                .loginPage("/login")
                        .loginPage("/login") // 设置登录页面，默认不进行校验 GET 请求
                        .loginProcessingUrl("/echologin") // 登录请求地址 POST 请求
                        // .usernameParameter("name").passwordParameter("password") // 设置默认用户名和密码参数名称
                        .successHandler(new LoginSuccessHandler("/main", true)) // 成功后跳转
                        .failureHandler(new LoginFailHandler("/loginfail"))) // 失败处理
                .logout(logout -> logout
                        .logoutUrl("/logout") // 退出登录请求地址
                        .logoutSuccessUrl("/login") // 退出成功后跳转地址，默认登录地址?logout
                        .logoutSuccessHandler(new EchoLogoutSuccessHandler()) // 设置退出成功后处理代码
                        .addLogoutHandler(new EchoLogoutHandler())) // 额外退出登录成功后处理代码
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/login", "/echologin", "/loginfail").permitAll() // 允许所有人访问
                        .anyRequest().authenticated()) // 其他所有请求都需要认证
                .csrf(csrf -> csrf.disable()) ;// 禁用CSRF保护

        return echo.build();
    }
 */

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests

                    .anyRequest().permitAll() // 允许所有请求
            )
            .csrf(csrf -> csrf.disable()); // 禁用CSRF保护
            //.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

}
