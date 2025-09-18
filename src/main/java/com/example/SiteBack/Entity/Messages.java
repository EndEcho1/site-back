package com.example.SiteBack.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Messages {

    private String content;

    private LocalDateTime createdAt;

    private boolean read = false;
}
