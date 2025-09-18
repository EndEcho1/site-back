package com.example.SiteBack.Entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "BaArticle")
public class BaArticle {

    @Id
    private String id;
    @Field("theme")
    @Indexed //普通索引
    private String theme;
    @Field("title")
    private String title;
    @Field("author")
    private Author author; // 评论作者信息
    @Field("content")
    private String content;
    @Field("tags")
    private List<String> tags;  // 标签列表
    @Field("like")
    private int likes;
    @Field("bad")
    private int bad;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;



}
