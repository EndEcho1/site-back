package com.example.siteback.Controller;

import com.example.siteback.DTO.BaArticleDTO;
import com.example.siteback.DTO.BaArticleSummaryDTO;
import com.example.siteback.DTO.BaCommentDTO;
import com.example.siteback.DTO.BaPagedResultDTO;
import com.example.siteback.Entity.Author;
import com.example.siteback.Entity.BaArticle;
import com.example.siteback.Service.BaArticleService;
import com.example.siteback.Service.BaUserService;
import com.example.siteback.jwt.JwtUtil;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BaArticleController {

    @Autowired
    BaArticleService baArticleService;

    @Autowired
    BaUserService baUserService;

    @Autowired
     JwtUtil jwtUtil;

    @GetMapping("/getArticleById/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable String id) {

        // 校验是否为合法的 ObjectId 格式
        if (!ObjectId.isValid(id)) {
            return ResponseEntity.badRequest().body("Invalid ObjectId format: " + id);
        }

        BaArticle article = new BaArticle();
        article = baArticleService.getArticleById(id);

        System.out.println("controller="+article);
        // 2. 返回响应
        return ResponseEntity.ok(article);
    }

    @GetMapping("/searchArticleByThemes")
    public ResponseEntity<?> searchArticleByThemes(
            @RequestParam(defaultValue = "") String keywords,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            BaPagedResultDTO<BaArticleSummaryDTO> results = baArticleService.searchThemeByKeywords(keywords, page, pageSize);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Search failed: " + e.getMessage());
        }
    }

    @GetMapping("/searchArticleByUserId")
    public ResponseEntity<?> searchArticleByUserId(
            String useId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // 1. 检查 Token 是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("未提供有效的Token");
        }

        try{

            BaPagedResultDTO<BaArticleSummaryDTO> results = baArticleService.searchArticleByUserId(useId);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(results);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("error:"+e.getMessage());
        }
    }




    @PostMapping ("/createArticle")
    public ResponseEntity<?> createArticle(
            @RequestBody BaArticleDTO baArticleDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {

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

            if(level == null || level < 1){
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("error");
            }


            String articleId = baArticleService.createArticle(userId, baArticleDTO);

            if (articleId == null || articleId.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("文章创建失败");
            }
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(articleId);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

    }


}
