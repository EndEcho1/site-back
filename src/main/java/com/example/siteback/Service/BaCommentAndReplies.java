package com.example.siteback.Service;

import com.example.siteback.DTO.BaCommentDTO;
import com.example.siteback.DTO.BaRepliesDTO;
import com.example.siteback.Entity.Author;
import com.example.siteback.Entity.BaComments;
import com.example.siteback.Entity.BaReplies;
import com.example.siteback.Entity.BaUser;
import com.example.siteback.Error.BaErrorException;
import com.example.siteback.Repository.BaCommentRepository;
import com.example.siteback.Repository.BaRepliesRepository;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaCommentAndReplies {

    @Autowired
    private BaUserService baUserService;

    @Autowired
    private BaCommentRepository baCommentRepository;

    @Autowired
    private BaRepliesRepository baRepliesRepository;

    public String createComment(String userId, BaCommentDTO baCommentDTO){


        try {
            Author author = baUserService.getAuthorById(userId);

            BaComments baComments = new BaComments();
            baComments.setArticleId(baCommentDTO.getArticleId());
            baComments.setContent(baCommentDTO.getContent());
            baComments.setAuthor(author);

            BaComments saveComments =  baCommentRepository.save(baComments);

            return saveComments.getId();

        }catch (BaErrorException e) {
            return e.getMessage();
        }

    }


    public  String createReplies(String userId, BaRepliesDTO baRepliesDTO){

        //Assert.notNull(baRepliesDTO.getReplyTo(),"reply is null");
        Assert.notNull(baRepliesDTO.getArticleId(),"need article ID");
        Assert.notNull(baRepliesDTO.getParentCommentId(),"need comment ID");


        try {
            Author author = baUserService.getAuthorById(userId);

            BaReplies baReplies = new BaReplies();
            baReplies.setBaCommentId(baRepliesDTO.getParentCommentId());
            baReplies.setContent(baRepliesDTO.getContent());
            baReplies.setAuthor(author);
            baReplies.setArticleId(baRepliesDTO.getArticleId());
            baReplies.setReplyTo(baRepliesDTO.getReplyTo());

            BaReplies saveReplies = baRepliesRepository.save(baReplies);

            return saveReplies.getId();
        }catch (BaErrorException e){
            return "user :"+e.getMessage();
        }
    }

}
