package com.example.siteback.Controller;

import com.example.siteback.DTO.BaCommentAndRepliesDTO;
import com.example.siteback.DTO.BaCommentDTO;
import com.example.siteback.DTO.BaRepliesDTO;
import com.example.siteback.Entity.Author;
import com.example.siteback.Entity.BaComments;
import com.example.siteback.Entity.BaReplies;
import com.example.siteback.Error.BaErrorException;
import com.example.siteback.Service.BaArticleService;
import com.example.siteback.Service.BaCommentAndReplies;
import com.example.siteback.jwt.JwtUtil;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BaCommentController {

    @Autowired
    BaArticleService baArticleService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    BaCommentAndReplies baCommentAndReplies;

    @GetMapping("/getComments/{articleId}")
    public ResponseEntity<?> getCommentByArticleId(@PathVariable String articleId) {
        if (!ObjectId.isValid(articleId)) {
            return ResponseEntity.badRequest().body("Invalid ObjectId format: " + articleId);
        }

        try {
            // 使用结构化并发获取评论和回复
            BaCommentAndRepliesDTO dto = baArticleService.getArticleDetailWithConcurrency(articleId);

            // 构建树形结构（仅基于baCommentId）
            List<CommentTreeNode> commentTree = buildFlatCommentTree(dto.getListComments(), dto.getListReplies());

            return ResponseEntity.ok(commentTree);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching comments: " + e.getMessage());
        }
    }

    // 构建扁平评论结构（评论 + 直接回复，去掉多层嵌套）
    private List<CommentTreeNode> buildFlatCommentTree(List<BaComments> comments, List<BaReplies> replies) {
        // 按评论ID分组回复
        Map<String, List<BaReplies>> repliesByCommentId = replies.stream()
                .collect(Collectors.groupingBy(BaReplies::getBaCommentId));

        // 构建评论节点（每个评论只有直接回复，不嵌套）
        return comments.stream().map(comment -> {
            CommentTreeNode node = new CommentTreeNode();
            node.setId(comment.getId());
            node.setArticleId(comment.getArticleId());
            node.setContent(comment.getContent());
            node.setAuthor(comment.getAuthor());
            node.setCreatedAt(comment.getCreatedAt());
            node.setUpdatedAt(comment.getUpdatedAt());
            node.setDeleted(comment.isDeleted());

            // 添加该评论的直接回复（但这些回复不会再嵌套别的 replies）
            List<CommentReplyNode> flatReplies = repliesByCommentId.getOrDefault(comment.getId(), Collections.emptyList())
                    .stream().map(reply -> {
                        CommentReplyNode replyNode = new CommentReplyNode();
                        replyNode.setId(reply.getId());
                        replyNode.setArticleId(reply.getArticleId());
                        replyNode.setContent(reply.getContent());
                        replyNode.setParentCommentId(reply.getBaCommentId());
                        replyNode.setAuthor(reply.getAuthor());
                        replyNode.setCreatedAt(reply.getCreatedAt());
                        replyNode.setUpdatedAt(reply.getUpdatedAt());
                        replyNode.setDeleted(reply.isDeleted());
                        replyNode.setReplyTo(reply.getReplyTo()); // 只用于指示回复目标
                        return replyNode;
                    }).collect(Collectors.toList());

            node.setReplies(flatReplies);
            return node;
        }).collect(Collectors.toList());
    }


    @Data
    public static class CommentTreeNode {
        private String id;
        private String articleId;
        private String content;
        private Author author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isDeleted;

        // 只包含直接回复，不嵌套
        private List<CommentReplyNode> replies = new ArrayList<>();
    }

    @Data
    public static class CommentReplyNode {
        private String id;
        private String articleId;
        private String parentCommentId; // ✅ 新增：指向所属评论
        private String content;
        private Author author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isDeleted;

        // 指向被回复的用户ID或评论ID（由前端解析展示）
        private String replyTo;
    }




    @PostMapping ("/createComment")
    public ResponseEntity<?> createComment(
            @RequestBody BaCommentDTO baCommentDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        // 1. 检查 Token 是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("未提供有效的Token");
        }

        // 2. 手动解析 Token（复用你的 JwtUtil）
        String token = authHeader.substring(7);
        try {

            String userId = jwtUtil.extractUserId(token);  // 从 Token 中提取用户 ID
            Integer level = jwtUtil.extractLevel(token);   // 提取用户等级（可选）

            if (level == null || level < 2) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("error from post");
            }

            String commentsId = baCommentAndReplies.createComment(userId,baCommentDTO);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(commentsId);

        }catch (BaErrorException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e+"error from service");
        }
    }

    @PostMapping("/createReplies")
    public  ResponseEntity<?>  createReplies(
            @RequestBody BaRepliesDTO baRepliesDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        // 1. 检查 Token 是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("未提供有效的Token");
        }

        // 2. 手动解析 Token（复用你的 JwtUtil）
        String token = authHeader.substring(7);
        try {

            String userId = jwtUtil.extractUserId(token);  // 从 Token 中提取用户 ID
            Integer level = jwtUtil.extractLevel(token);   // 提取用户等级（可选）

            if (level == null || level < 2) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("error from post");
            }

            String repliesId = baCommentAndReplies.createReplies(userId,baRepliesDTO);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(repliesId);

        }catch (BaErrorException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e+"error from service");
        }


    }













}
