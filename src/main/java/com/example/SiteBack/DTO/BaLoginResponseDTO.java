package com.example.SiteBack.DTO;

import lombok.Data;

@Data
public class BaLoginResponseDTO {
    private String token;
    private int level;

    public BaLoginResponseDTO(String token, int level) {
        this.token = token;
        this.level = level;
    }
}
