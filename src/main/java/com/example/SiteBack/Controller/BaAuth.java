package com.example.SiteBack.Controller;

import com.example.SiteBack.DTO.BaLoginDTO;
import com.example.SiteBack.DTO.BaLoginResponseDTO;
import com.example.SiteBack.DTO.BaRegisterDTO;
import com.example.SiteBack.DTO.BaRegisterResponseDTO;
import com.example.SiteBack.Entity.BaUser;
import com.example.SiteBack.Error.BaErrorException;
import com.example.SiteBack.Service.BaAuthService;
import com.example.SiteBack.Service.BaUserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class BaAuth {

    private final BaAuthService baAuthService;

    private final BaUserService baUserService;

    public BaAuth(BaAuthService baAuthService, BaUserService baUserService) {
        this.baAuthService = baAuthService;
        this.baUserService = baUserService;
    }


    // 登录接口
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody BaLoginDTO loginRequest, BindingResult bindingResult) {
        // 数据验证失败时返回错误信息
        System.out.println(loginRequest);

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .reduce((message1, message2) -> message1 + "; " + message2)
                    .orElse("Validation error");
            return new ResponseEntity<>(new BaLoginResponseDTO(errorMessage, 99), HttpStatus.BAD_REQUEST);
        }

        // 调用服务层进行登录验证
        String token = baAuthService.login(loginRequest.getLoginIdentifier(), loginRequest.getPassword());

        // 登录成功后获取用户权限
        Optional<BaUser> BaUser = baUserService.getUser(loginRequest.getLoginIdentifier());
        int Level = BaUser.get().getLevel();

        System.out.println(token);
        if (Level != 0) {
            // 返回包含 token 和用户权限等级的响应

            return new ResponseEntity<>(new BaLoginResponseDTO(token, Level), HttpStatus.OK);

        } else {
            // 无权限状态
            return new ResponseEntity<>(new BaLoginResponseDTO(token, 99), HttpStatus.FORBIDDEN);
        }
    }


    @PostMapping("/register") //<?>表示通配符，即返回的响应体类型可以是任意类型
    public ResponseEntity<?> register(@Valid @RequestBody BaRegisterDTO loginRequest, BindingResult bindingResult) {
        // 数据验证失败时返回错误信息
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .reduce((message1, message2) -> message1 + "; " + message2)
                    .orElse("Register error");
            return new ResponseEntity<>(new BaRegisterResponseDTO(errorMessage, 98), HttpStatus.BAD_REQUEST);
        }

        return baUserService.getUser(loginRequest.getRegisterIdentifier())
                .map(existingUser ->
                        new ResponseEntity<>(
                                new BaRegisterResponseDTO("User already exists", 97),
                                HttpStatus.CONFLICT  // 改用 409 Conflict
                        )
                )
                .orElseGet(() -> {

                    try {
                        BaUser baUser = baAuthService.registerUser(loginRequest);

                        return new ResponseEntity<>(
                                new BaRegisterResponseDTO("Registration successful", 0),
                                HttpStatus.OK
                        );
                    }catch (BaErrorException e) {
                        return new ResponseEntity<>(
                                new BaRegisterResponseDTO("Invalid data: " + e.getMessage(), 98),
                                HttpStatus.BAD_REQUEST
                        );
                    }



                });
    }
}
