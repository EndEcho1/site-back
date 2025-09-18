package com.example.SiteBack.Entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List; // List 接口

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

//按下 Alt + Insert

@Data
@Document(collection = "BaUser")
@CompoundIndexes({
        @CompoundIndex(name = "username_idx"/*只是这个索引列表的名称主要用于数据库管理和调试，帮助你在查看和管理索引时更容易辨识*/,
                def = "{'username': 1}", unique = true /*值唯一*/ ),//创建一个索引列表合集，也就是说，MongoDB 会对 username 字段进行排序存储，加快查询
        @CompoundIndex(name = "email_idx", def = "{'email': 1}", unique = true),
})
public class BaUser {

    @Id
    private ObjectId id;
    @Field("user_id")
    private String userId;
    @Field("username")
    private String username;
    @Field("password")
    private String password;
    @Field("email")
    private String email;
    @Field("phone_number")
    private String phoneNumber;
    @Field("bro")
    private String bro ;
    @Field("friendList")
    private List<String> friendList = new ArrayList<>();
    @Field("articleList")
    private List<String> articleList = new ArrayList<>();
    @Field("level")
    private int level;
    @Field("coins")
    private int coins;
    @Field("url")
    private String url;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    //private List<String> roles = new ArrayList<>(); // 防止 null;

}