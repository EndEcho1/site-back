package com.example.siteback.Entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "BaChat")
@CompoundIndex(def = "{'receiverId': 1, 'messages.read': 1}")
public class BaChat {

    @Id
    private String id;

    @Field("senderId")
    private String senderId;

    @Field("receiverId")
    private String receiverId;

    @Field("messages")
    private List<Messages> messages = new ArrayList<>();


    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
