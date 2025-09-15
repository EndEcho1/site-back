package com.example.siteback.DTO;

import com.example.siteback.Entity.Author;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaCommentDTO {

    private String articleId; // 关联文章ID

    private Author author; // 评论作者信息

    private String content;//内容


}
