package com.example.SiteBack.Service;

import com.example.SiteBack.DTO.BaCommentDTO;
import com.example.SiteBack.DTO.BaRepliesDTO;
import com.example.SiteBack.Entity.Author;
import com.example.SiteBack.Entity.BaComments;
import com.example.SiteBack.Entity.BaReplies;
import com.example.SiteBack.Error.BaErrorException;
import com.example.SiteBack.Repository.BaCommentRepository;
import com.example.SiteBack.Repository.BaRepliesRepository;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
