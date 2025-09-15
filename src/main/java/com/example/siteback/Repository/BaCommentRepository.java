package com.example.siteback.Repository;

import com.example.siteback.Entity.BaComments;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface BaCommentRepository extends MongoRepository<BaComments,String> {

    List<BaComments> findByArticleId(String articleId);



}
