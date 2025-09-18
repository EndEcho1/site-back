package com.example.siteback.jwt;

import com.example.siteback.Error.BaErrorException;
import com.example.siteback.Error.BaErrorMessagesE;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component // 将这个类标记为一个 Spring 组件
public class JwtUtil {


    private final JwtKeyManager keyManager;

    public JwtUtil(JwtKeyManager keyManager) {
        this.keyManager = keyManager;
    }
    //获取权限
    public Integer extractLevel(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("level", Integer.class);
    }
    // 从令牌中提取用户id
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 从令牌中提取声明信息
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 从令牌中解析所有声明信息
    private Claims extractAllClaims(String token) {
        try {
        return Jwts.parser()
                .setSigningKey(keyManager.getPublicKey())
                .build()
                .parseClaimsJws(token)              // 解析 token
                .getBody();                         // 获取 claims
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token已过期", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Token格式错误", e);
        } catch (Exception e) {
            throw new RuntimeException("Token解析失败", e);
        }
    }

    // 检查令牌是否过期
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 从令牌中提取过期时间
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 验证令牌是否有效
    public Boolean validateToken(String token, String username,Integer level) {
        final String extractedUsername = extractUserId(token);  // 提取 token 中的 username
        final Integer extractedLevel = extractLevel(token);      // 提取 token 中的 level

        // 空值检查
        if (extractedUsername == null || extractedLevel == null) {
            throw new BaErrorException(BaErrorMessagesE.INVALID_INPUT);

        }
        // 比较 username 和 level 是否一致，且验证 token 是否过期
        return (extractedUsername.equals(username)
                && extractedLevel.equals(level)
                && !isTokenExpired(token));
    }

    // 生成令牌
    public String generateToken(String userid,int level) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userid,level);
    }

    // 创建令牌
    private String createToken(Map<String, Object> claims, String subject,int level ) {
        claims.put("level",level);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 设置过期时间为 10 小时
                .signWith(keyManager.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }
}


