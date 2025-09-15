package com.example.siteback.DTO;

import com.example.siteback.Entity.Author;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaArticleDTO {

    private String theme;

    private Author author;

    private String content;

    private List<String> tags;  // 标签列表
}
