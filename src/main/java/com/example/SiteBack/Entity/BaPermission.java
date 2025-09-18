package com.example.SiteBack.Entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "BaPermission")
public class BaPermission {

    @Field("level")
    private int level;
    @Field("Permission")
    private String permission;

    @DBRef // 直接关联 User 实体
    private BaUser user;

    public void  setLevel(int level) {this.level = level;}
    public void  setPermission(String permission) {this.permission = permission;}

}
