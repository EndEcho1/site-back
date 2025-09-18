package com.example.SiteBack.Entity;


import com.example.SiteBack.Repository.BaHasAuthor;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "BaReplies")
public class BaReplies implements BaHasAuthor {

    @Id
    private String id;
    @Field("parentCommentId")
    private String baCommentId;// 关联评论ID
    @Field("articleId")
    private String articleId; // 关联文章ID
    @Field("content")
    private String content;//内容
    @Field("author")
    private Author author; // 评论作者信息
    @Field("replyTo")
    private String replyTo; //回复对象ID

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
;
    private boolean isDeleted;

    @Override
    public Author getAuthor() {
        return this.author;
    }

}
