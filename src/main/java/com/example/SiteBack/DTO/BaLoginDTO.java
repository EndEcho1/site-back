package com.example.SiteBack.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class BaLoginDTO {
    @NotEmpty(message = "Login identifier cannot be empty")
    private String loginIdentifier;  // 可以是用户名、邮箱、手机号

    @NotEmpty(message = "Password cannot be empty")
    private String password;


}
