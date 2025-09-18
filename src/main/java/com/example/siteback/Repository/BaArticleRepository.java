package com.example.siteback.Repository;

import com.example.siteback.Entity.BaArticle;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BaArticleRepository extends MongoRepository<BaArticle, String> {

     Optional<BaArticle>  findById (@NotNull String id);

     List<BaArticle> findByThemeLike(String theme);
     // Repository 方法

     List<BaArticle> findByTitleLike(String title);

     @Query("{ \"$text\": { \"$search\": ?0 } }")
     List<BaArticle> findByTitleText(String titleText);



}
