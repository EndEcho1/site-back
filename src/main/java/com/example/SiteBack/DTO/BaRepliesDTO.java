package com.example.SiteBack.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaRepliesDTO {

    private String parentCommentId;// 关联评论ID

    private String replyTo; //回复对象ID

    private String articleId; // 关联文章ID

    private String content;//内容





}
