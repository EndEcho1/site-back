package com.example.siteback.Repository;

import com.example.siteback.Entity.BaReplies;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BaRepliesRepository extends MongoRepository<BaReplies,String> {

    List<BaReplies> findByBaCommentId(String baCommentId);

    // 使用 $in 操作符一次查询多个评论ID对应的评论
    List<BaReplies> findByBaCommentIdIn(List<String> baCommentId);
}
