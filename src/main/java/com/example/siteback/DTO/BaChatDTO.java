package com.example.siteback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaChatDTO {

    private String receiverId;

    private String content;

    private LocalDateTime createdAt;


}
