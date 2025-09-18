package com.example.siteback.Service;

import com.example.siteback.DTO.Article.BaArticleDTO;
import com.example.siteback.DTO.Article.BaArticleSummaryDTO;
import com.example.siteback.DTO.Article.BaCommentAndRepliesDTO;
import com.example.siteback.DTO.BaPagedResultDTO;
import com.example.siteback.Entity.*;
import com.example.siteback.Repository.BaArticleRepository;
import com.example.siteback.Repository.BaCommentRepository;
import com.example.siteback.Repository.BaRepliesRepository;
import com.example.siteback.Repository.BaUserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaArticleService {

    @Autowired
    private MongoTemplate mongoTemplate;// db 查询注入

    @Autowired
    private ExecutorService virtualThreadExecutor; // 注入虚拟线程执行器

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;//注入redis
    @Autowired
    private BaArticleRepository baArticleRepository;
    @Autowired
    private BaCommentRepository baCommentRepository;
    @Autowired
    private BaRepliesRepository baRepliesRepository;
    @Autowired
    private BaUserService baUserService;
    @Autowired
    private BaUserRepository baUserRepository;

    private static final String ARTICLE_CACHE_PREFIX = "article:";  // 缓存的前缀

    private final Map<String, ObjectId> idCache = new ConcurrentHashMap<>();//缓存objectId



    public BaArticle getArticleById(String id) {
        // 1. 尝试从 Redis 中获取文章
        BaArticle baArticle = null;
        String cacheKey = ARTICLE_CACHE_PREFIX + id;
        baArticle = (BaArticle) redisTemplate.opsForValue().get(cacheKey);

        // 2. 如果 Redis 中没有，查询 MongoDB 并缓存到 Redis
        if (baArticle == null) {
            baArticle = baArticleRepository.findById(id).orElse(null);

            if (baArticle != null) {
                try {
                    redisTemplate.opsForValue().set(cacheKey, baArticle, 1, TimeUnit.HOURS);
                } catch (Exception e) {
                    log.warn("Redis 缓存失败：{}", e.getMessage());
                }
            }
        }

        // 3. 动态填充作者头像 URL（走缓存）
        if (baArticle != null && baArticle.getAuthor() != null) {
            String authorId = baArticle.getAuthor().getAuthorId();
            String avatarUrl = baUserService.getAuthorAvatarUrlById(authorId); // 带缓存
            baArticle.getAuthor().setUrl(avatarUrl);
        }

        // 4. 返回文章
        return baArticle;
    }



    public BaPagedResultDTO<BaArticleSummaryDTO> searchThemeByKeywords(@NotNull String keywords, int page, int pageSize) {
        // 1. 分词
        String[] keywordArray = keywords.trim().split("\\s+");

        // 2. 查询所有命中文章
        List<Criteria> orCriteriaList = new ArrayList<>();
        for (String keyword : keywordArray) {
            orCriteriaList.add(Criteria.where("theme").regex(keyword, "i"));
            orCriteriaList.add(Criteria.where("title").regex(keyword, "i"));
            orCriteriaList.add(Criteria.where("tags").regex(keyword, "i"));
        }
        Query query = new Query(new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0])));
        List<BaArticle> articles = mongoTemplate.find(query, BaArticle.class);

        // 3. 计算权重 & 排序
        List<BaArticleWithScore> scoredArticles = articles.stream().map(article -> {
                    int score = 0;
                    for (String keyword : keywordArray) {
                        if (article.getTheme() != null && article.getTheme().toLowerCase().contains(keyword.toLowerCase())) {
                            score++;
                        }
                        if (article.getTitle() != null && article.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                            score++;
                        }
                        if (article.getTags() != null) {
                            for (String tag : article.getTags()) {
                                if (tag.toLowerCase().contains(keyword.toLowerCase())) {
                                    score++;
                                    break;
                                }
                            }
                        }
                    }
                    return new BaArticleWithScore(article, score);
                }).filter(item -> item.score > 0)
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .collect(Collectors.toList());

        // 4. 分页
        long totalCount = scoredArticles.size(); // 总条数
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, scoredArticles.size());

        List<BaArticleWithScore> pagedArticles = fromIndex >= scoredArticles.size()
                ? Collections.emptyList()
                : scoredArticles.subList(fromIndex, toIndex);

        List<BaArticleSummaryDTO> items = pagedArticles.stream()
                .map(item -> new BaArticleSummaryDTO(
                        item.article.getId(),
                        item.article.getAuthor(),
                        item.article.getTheme(),
                        item.article.getTags()
                ))
                .collect(Collectors.toList());

        return new BaPagedResultDTO<>(items, totalCount);
    }



    public BaPagedResultDTO<BaArticleSummaryDTO> searchArticleByUserId(@NotNull String userId) {
        // 1. 查询指定作者的所有文章
        Criteria criteria = Criteria.where("author.authorId").is(userId);
        Query query = new Query(criteria);
        List<BaArticle> articles = mongoTemplate.find(query, BaArticle.class);

        // 2. 排序（将 createdAt 为空的排在最后）
        List<BaArticle> sortedArticles = articles.stream()
                .sorted((a, b) -> {
                    LocalDateTime aDate = a.getCreatedAt();
                    LocalDateTime bDate = b.getCreatedAt();
                    if (aDate == null && bDate == null) return 0;
                    if (aDate == null) return 1;
                    if (bDate == null) return -1;
                    return bDate.compareTo(aDate); // 降序
                })
                .collect(Collectors.toList());

        // 3. 转为简要 DTO
        List<BaArticleSummaryDTO> items = sortedArticles.stream()
                .map(article -> new BaArticleSummaryDTO(
                        article.getId(),
                        article.getAuthor(),
                        article.getTheme(),
                        article.getTags()
                ))
                .collect(Collectors.toList());

        return new BaPagedResultDTO<>(items, items.size());
    }



    public List<BaComments> getComments(String articleId){
        List<BaComments> comments = null;
        //String cacheKey = ARTICLE_CACHE_PREFIX + articleId;
        // 尝试从 Redis 中获取缓存的评论列表
        //List<BaComments> comments = (List<BaComments>) redisTemplate.opsForValue().get(cacheKey);


        // 如果缓存中没有，则从数据库查询
        if (comments == null || comments.isEmpty()) {
            // 进行数据库查询并缓存
            //存缓等待实现
            comments = baCommentRepository.findByArticleId(articleId);


        }

        return comments;
    }

    public List<BaReplies> getReplies(String baCommentId){
        String cacheKey = ARTICLE_CACHE_PREFIX + baCommentId;

        // 尝试从 Redis 中获取缓存的评论列表
        List<BaReplies> replies = (List<BaReplies>) redisTemplate.opsForValue().get(cacheKey);

        // 如果缓存中没有，则从数据库查询
        if (replies == null || replies.isEmpty()) {
            // 进行数据库查询并缓存
            //存缓等待实现
            replies= baRepliesRepository.findByBaCommentId(baCommentId);


        }

        return replies;
    }


    // 异步获取评论 - 使用虚拟线程
    public CompletableFuture<List<BaComments>> getCommentsAsync(String articleId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Query query = new Query(Criteria.where("articleId").is(articleId));
                return mongoTemplate.find(query, BaComments.class);
            } catch (Exception e) {
                log.error("Failed to fetch comments for article {}", articleId, e);
                throw new CompletionException(e);
            }
        }, virtualThreadExecutor);
    }

    // 异步获取所有相关回复 - 虚拟线程
    public CompletableFuture<List<BaReplies>> getRepliesForCommentsIdAsync(List<String> baCommentIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Query query = new Query(Criteria.where("baCommentId").in(baCommentIds));
                return mongoTemplate.find(query, BaReplies.class);
            } catch (Exception e) {
                log.error("Failed to fetch replies for comments {}", baCommentIds, e);
                throw new CompletionException(e);
            }
        }, virtualThreadExecutor);
    }

    // 使用结构化并发同时获取评论和回复
    public BaCommentAndRepliesDTO getArticleDetailWithConcurrency(String articleId)
            throws ExecutionException, InterruptedException {

        // 1. 异步获取评论
        CompletableFuture<List<BaComments>> commentsFuture = getCommentsAsync(articleId);

        // 2. 获取评论后异步获取回复
        CompletableFuture<List<BaReplies>> repliesFuture = commentsFuture.thenCompose(comments -> {
            List<String> commentIds = comments.stream()
                    .map(BaComments::getId)
                    .toList();
            return getRepliesForCommentsIdAsync(commentIds);
        });

        // 3. 等待所有完成
        CompletableFuture.allOf(commentsFuture, repliesFuture).join();

        List<BaComments> comments = commentsFuture.join();
        List<BaReplies> replies = repliesFuture.join();

        baUserService.enrichAuthorsWithAvatarUrl(comments);
        baUserService.enrichAuthorsWithAvatarUrl(replies);

        return new BaCommentAndRepliesDTO(comments, replies);

    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED) // 显式声明不支持事务
    public String createArticle (String userId, BaArticleDTO baArticleDTO){


        try{



            BaArticle baArticle = new BaArticle();
            baArticle.setTheme(baArticleDTO.getTheme());
            baArticle.setTitle("默认");
            baArticle.setAuthor(baArticleDTO.getAuthor()); // 填充作者
            baArticle.setContent(baArticleDTO.getContent());
            baArticle.setTags(baArticleDTO.getTags());
            baArticle.setLikes(0);
            baArticle.setBad(0);

            //保存
            BaArticle savedArticle = baArticleRepository.save(baArticle);

            BaUser baUser = baUserService.getUserById(userId);
            // 更新用户的文章列表
            baUser.getArticleList().add(savedArticle.getId());

            baUserRepository.save(baUser); // 刷新用户数据

            return savedArticle.getId();
        }catch (Exception e){
            e.printStackTrace(); // 打印异常堆栈
            return "user error"+e.getMessage();
        }






    }







    // 内部类，用来携带打分结果
    static class BaArticleWithScore {
        BaArticle article;
        int score;

        BaArticleWithScore(BaArticle article, int score) {
            this.article = article;
            this.score = score;
        }
    }

}
