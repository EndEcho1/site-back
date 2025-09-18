package com.example.SiteBack.Service;

import com.example.SiteBack.DTO.BaRegisterDTO;
import com.example.SiteBack.Entity.BaUser;
import com.example.SiteBack.Error.BaErrorException;
import com.example.SiteBack.Error.BaErrorMessagesE;
import com.example.SiteBack.Repository.BaUserRepository;
import com.example.SiteBack.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BaAuthService {

    @Autowired
    private BaUserRepository baUserRepository;  // 通过 @Autowired 注入 baUserRepository

    @Autowired
    private BaUserService baUserService;  // 注入 BaUserService 来查询密码

    @Autowired
    private JwtUtil jwtUtil;  // 注入 JWT 生成器

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 登录验证
    public String login(String loginIdentifier, String password) {
        // 获取用户密码
        BaUser user = baUserService.getUser(loginIdentifier)
                .orElseThrow(() -> new BaErrorException(BaErrorMessagesE.DATABASE_ERROR));


        String storedPassword = user.getPassword();
        int Level = user.getLevel();

        // 比对密码
        if (!password.equals(storedPassword)) {
            throw new BaErrorException(BaErrorMessagesE.INVALID_INPUT);
        }

        String userId = user.getId().toString();
        // 密码验证成功，生成 token
        return jwtUtil.generateToken(userId,Level);
    }

    /*public String login(String loginIdentifier, String rawPassword) {
        // 1. 获取用户（包含加密后的密码）
        BaUser user = baUserService.getUser(loginIdentifier)
                .orElseThrow(() -> new BaErrorException());

        // 2. 安全比对密码（使用passwordEncoder）
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BaErrorException(BaErrorMessagesE.INVALID_INPUT);
        }

        // 3. 生成Token（建议用userId代替loginIdentifier）
        return jwtUtil.generateToken(user.getUserId());
    }*/



    // 注册实现
    public BaUser registerUser(BaRegisterDTO baRegisterDTO){


        // 检查用户名、邮箱、手机号是否已存在
        if (baUserRepository.existsByUsername(baRegisterDTO.getRegisterIdentifier())) {
            throw new BaErrorException(BaErrorMessagesE.INVALID_INPUT);
        }
        if (baUserRepository.existsByEmail(baRegisterDTO.getRegisterIdentifier())) {
            throw new BaErrorException(BaErrorMessagesE.INVALID_INPUT);
        }
        if (baUserRepository.existsByPhoneNumber(baRegisterDTO.getRegisterIdentifier())) {
            throw new BaErrorException(BaErrorMessagesE.INVALID_INPUT);
        }
        // 创建用户对象
        BaUser bauser = new BaUser();
        bauser.setUsername(baRegisterDTO.getRegisterIdentifier());
        bauser.setEmail(baRegisterDTO.getRegisterIdentifier());
        bauser.setPhoneNumber(baRegisterDTO.getRegisterIdentifier());
        bauser.setPassword(passwordEncoder.encode(baRegisterDTO.getPassword())); // 加密密码


        // 保存用户信息
        return baUserRepository.save(bauser);

    }

}
