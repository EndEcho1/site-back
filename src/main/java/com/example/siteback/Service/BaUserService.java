package com.example.siteback.Service;

import com.example.siteback.DTO.BaUserAvatarDTO;
import com.example.siteback.Entity.Author;
import com.example.siteback.Entity.BaUser;
import com.example.siteback.Error.BaErrorException;
import com.example.siteback.Error.BaErrorMessagesE;
import com.example.siteback.Repository.BaHasAuthor;
import com.example.siteback.Repository.BaUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Service
public class BaUserService {

    @Autowired
    private BaUserRepository baUserRepository;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;//注入redis

    private static final String AUTHOR_AVATAR_CACHE_PREFIX = "author:avatar:";
    // 创建一个新用户并保存到数据库中
    public BaUser createUser(BaUser user) {
        // 使用baUserRepository的save方法保存用户
        return baUserRepository.save(user);
    }

    // 实现- 查询用户名 返回用户对象
    public Optional<BaUser> getUser(String loginIdentifier) {
        return baUserRepository.findByUsername(loginIdentifier)
                .or(() -> baUserRepository.findByEmail(loginIdentifier))
                .or(() -> baUserRepository.findByPhoneNumber(loginIdentifier));
    }

    public BaUser getUserById(String userid){

        ObjectId objectId = new ObjectId(userid);

        Optional<BaUser> baUserOpt = baUserRepository.findById(objectId);


        return  baUserOpt.orElse(null);
    }


    //实现- 查询一个一组用户头像
    public List<BaUserAvatarDTO> findUserAvatarsByIds(List<String> ids) {
        System.out.println("【数据库查】用户头像 ids: " + ids);

        //  把String id 转成 ObjectId
        List<ObjectId> objectIds = ids.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<BaUser> users = baUserRepository.findAllById(objectIds);

        return users.stream()
                .map(user -> new BaUserAvatarDTO(user.getId().toString(), user.getUrl()))
                .collect(Collectors.toList());
    }

    //转换- BaUserAvatarDTO -> Map
    public Map<String, String> getAvatarUrlsByAuthorIds(Set<String> authorIds) {
        // 1. 调用现有方法查询
        List<BaUserAvatarDTO> avatars = findUserAvatarsByIds(new ArrayList<>(authorIds));

        // 2. 转成 Map 方便填充
        return avatars.stream()
                .collect(Collectors.toMap(BaUserAvatarDTO::getId, BaUserAvatarDTO::getUrl));
    }

    //实现- 自动填充 评论&回复者 头像url
    public void enrichAuthorsWithAvatarUrl(List<? extends BaHasAuthor> items) {
        // 1. 收集所有唯一的 authorId
        Set<String> authorIds = items.stream()
                .map(item -> item.getAuthor().getAuthorId())
                .collect(Collectors.toSet());

        // 2. 查询所有头像URL（带缓存）
        Map<String, String> idToUrlMap = getAvatarUrlsByAuthorIds(authorIds);

        // 3. 把头像url填充回去
        items.forEach(item -> {
            Author author = item.getAuthor();
            String url = idToUrlMap.get(author.getAuthorId());
            if (url != null) {
                author.setUrl(url);
            }else {
                author.setUrl("https://example.com/default-avatar.png"); // 设置默认头像
            }
        });
    }



    //实现-查询一个用户头像
    public String findUserAvatarUrlById(String id) {

        // 把 String id 转成 ObjectId
        ObjectId objectId = new ObjectId(id);

        // 查询用户
        Optional<BaUser> userOpt = baUserRepository.findById(objectId);

        // 直接返回 url
        return userOpt.map(BaUser::getUrl).orElse(null);
    }



    public String getAuthorAvatarUrlById(String authorId) {
        String cacheKey = AUTHOR_AVATAR_CACHE_PREFIX + authorId;

        // 1. 先查 Redis 缓存
        String avatarUrl = (String) redisTemplate.opsForValue().get(cacheKey);
        if (avatarUrl != null) {
            System.out.println("【缓存命中】作者头像 authorId: " + authorId);
            return avatarUrl;
        }

        // 2. 查数据库
        System.out.println("【数据库查】作者头像 authorId: " + authorId);
        avatarUrl = findUserAvatarUrlById(authorId); //

        // 3. 放入 Redis 缓存
        if (avatarUrl != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, avatarUrl, 24, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("作者头像缓存失败：{}", e.getMessage());
            }
        }

        return avatarUrl;
    }



    //通过Id获取一个作者对象
    public Author getAuthorById (String userId){

        BaUser baUser = getUserById(userId);
        if (baUser == null) {
            throw new BaErrorException(BaErrorMessagesE.INVALID_INPUT);
        }

        Author author = new Author();
        author.setAuthorId(userId);
        author.setAuthorName(baUser.getUsername());

        return author;
    }


    // 获取所有用户的列表
    public List<BaUser> getAllUsers() {
        // 使用baUserRepository的findAll方法获取所有用户
        return baUserRepository.findAll();
    }

    // 更新用户信息
    public BaUser updateUser(BaUser user) {
        // 使用baUserRepository的save方法更新用户
        return baUserRepository.save(user);
    }

    // 根据用户ID删除用户
    public void deleteUser(String userId) {
        if (!ObjectId.isValid(userId)) {
            throw new IllegalArgumentException("无效的用户ID: " + userId);
        }
        // 使用baUserRepository的deleteById方法删除用户
        ObjectId objectId = new ObjectId(userId);
        baUserRepository.deleteById(objectId);
    }



}

