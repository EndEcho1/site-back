package com.example.siteback.DTO.Article;

import com.example.siteback.Entity.Author;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaArticleSummaryDTO {
    @Id
    private String id;      // 文章ID

    private Author author; // 评论作者信息

    private String theme;   // 主题 / 标题

    private List<String> tags;  // 标签列表

}
