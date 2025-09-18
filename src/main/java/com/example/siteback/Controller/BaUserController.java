package com.example.siteback.Controller;


import com.example.siteback.DTO.BaUserAvatarDTO;
import com.example.siteback.DTO.BaUserDTO;
import com.example.siteback.Entity.BaUser;
import com.example.siteback.Error.BaErrorException;
import com.example.siteback.Service.BaUserService;
import com.example.siteback.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BaUserController {

    @Autowired
    BaUserService baUserService;

    @Autowired
    JwtUtil jwtUtil;
    @GetMapping("/getUserAvatar")
    public List<BaUserAvatarDTO> getUserAvatarByIds(@RequestParam String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        return baUserService.findUserAvatarsByIds(idList);
    }

    @GetMapping("/getUser")
    public ResponseEntity<?> getUserByToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        // 1. 检查 Token 是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("未提供有效的Token");
        }

        // 2. 手动解析 Token（复用你的 JwtUtil）
        String token = authHeader.substring(7);

        try {
            String userId = jwtUtil.extractUserId(token);
            BaUser baUser = baUserService.getUserById(userId);
            System.out.println("take object to fr");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new BaUserDTO(baUser));

        }catch (BaErrorException e){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Error:"+e.getMessage());
        }


        }

}
