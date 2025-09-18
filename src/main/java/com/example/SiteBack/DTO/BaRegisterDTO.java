package com.example.SiteBack.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class BaRegisterDTO {
    @NotEmpty(message = "Login identifier cannot be empty")
    private String registerIdentifier;  // 可以是用户名、邮箱、手机号

    @NotEmpty(message = "Password cannot be empty")
    private String password;

    @NotEmpty(message = "Confirm Identity")
    private boolean isSpecialIndividual;
}
