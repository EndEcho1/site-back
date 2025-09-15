package com.example.siteback.Handler;

import com.example.siteback.DTO.BaChatDTO;
import com.example.siteback.Entity.BaChat;
import com.example.siteback.Entity.Messages;
import com.example.siteback.jwt.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class BaChatWebSocketHandler extends TextWebSocketHandler {



    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper; // 用于JSON转换

    private final JwtUtil jwtUtil;

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, List<Messages>>> tempMessageCache = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接建立，sessionId: {}", session.getId());

        // 从握手请求的URI获取token（Servlet WebSocket版本）
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        String token = Arrays.stream(Optional.ofNullable(query).orElse("").split("&"))
                .filter(p -> p.startsWith("token="))
                .map(p -> p.substring(6))
                .findFirst()
                .orElse(null);

        if (token == null) {
            session.close();
            return;
        }

        String userId = jwtUtil.extractUserId(token);
        tempMessageCache.put(userId, new HashMap<>());
        sessions.put(userId, session);

        pushUnreadMessages(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String userId = findUserIdBySession(session);
        if (userId == null) {
            log.warn("未找到对应用户，sessionId: {}", session.getId());
            return;
        }

        try {
            BaChatDTO dto = objectMapper.readValue(payload, BaChatDTO.class);
            handleIncomingMessage(userId, dto);
        } catch (JsonProcessingException e) {
            session.sendMessage(new TextMessage("消息格式错误"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = findUserIdBySession(session);
        if (userId != null) {
            persistMessages(userId);
            sessions.remove(userId);
            tempMessageCache.remove(userId);
        }
        log.info("WebSocket连接关闭，sessionId: {}", session.getId());
    }


    //实现暂时存储消息
    private void cacheMessage(String senderId, String receiverId, Messages message) {
        tempMessageCache
                .computeIfAbsent(senderId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(receiverId, k -> new ArrayList<>())
                .add(message);
    }


    private void persistMessages(String senderId) {
        Map<String, List<Messages>> userCache = tempMessageCache.get(senderId);

        if (userCache == null) return;

        userCache.forEach((receiverId, messages) -> {
            // 查询BaChat会话
            BaChat chat = mongoTemplate.findOne(
                    Query.query(Criteria.where("senderId").is(senderId).and("receiverId").is(receiverId)),
                    BaChat.class
            );

            if (chat == null) {
                // 新会话
                chat = new BaChat();
                chat.setSenderId(senderId);
                chat.setReceiverId(receiverId);
                chat.setMessages(new ArrayList<>(messages));
                chat.setUpdatedAt(LocalDateTime.now());
            } else {
                // 追加消息
                chat.getMessages().addAll(messages);
                chat.setUpdatedAt(LocalDateTime.now());
            }

            mongoTemplate.save(chat);
        });
    }

    private void handleIncomingMessage(String senderId, BaChatDTO dto) throws IOException {
        String receiverId = dto.getReceiverId();
        String content = dto.getContent();
        if (receiverId == null || receiverId.isEmpty()) {
            log.warn("Received message with null or empty receiverId from senderId={}", senderId);
            return;
        }
        LocalDateTime createdAt = dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now();

        Messages message = new Messages(content, createdAt, false);

        WebSocketSession toSession = sessions.get(receiverId);
        if (toSession != null && toSession.isOpen()) {
            long timestamp = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            String payload = String.format("{\"from\":\"%s\", \"message\":\"%s\", \"timestamp\":%d}",
                    senderId, content.replace("\"", "\\\""), timestamp);
            message.setRead(true);

            toSession.sendMessage(new TextMessage(payload));
            cacheMessage(senderId, receiverId, message);
        } else {
            cacheMessage(senderId, receiverId, message);
        }
    }



    public void pushUnreadMessages(String userId, WebSocketSession session) throws IOException {
        // 1. 从MongoDB查询所有收件人为该用户的聊天记录
        List<BaChat> dbChats = mongoTemplate.find(
                Query.query(Criteria.where("receiverId").is(userId)),
                BaChat.class
        );

        List<BaChat> unreadChats = new ArrayList<>();

        for (BaChat chat : dbChats) {
            List<Messages> unread = chat.getMessages().stream()
                    .filter(m -> !m.isRead())
                    .collect(Collectors.toList());

            if (!unread.isEmpty()) {
                // 拷贝用于发送给前端
                BaChat clone = new BaChat();
                clone.setSenderId(chat.getSenderId());
                clone.setReceiverId(chat.getReceiverId());
                clone.setMessages(unread);
                clone.setUpdatedAt(chat.getUpdatedAt());
                unreadChats.add(clone);

                // 原始消息标记为已读
                for (Messages message : chat.getMessages()) {
                    if (!message.isRead()) {
                        message.setRead(true);
                    }
                }
                chat.setUpdatedAt(LocalDateTime.now());
                mongoTemplate.save(chat);
            }
        }

        // 2. 查找缓存中未读消息（即暂存的 tempMessageCache）
        Map<String, List<Messages>> userCache = tempMessageCache.get(userId);
        if (userCache != null) {
            for (Map.Entry<String, List<Messages>> entry : userCache.entrySet()) {
                String senderId = entry.getKey();
                List<Messages> messages = entry.getValue();

                if (!messages.isEmpty()) {
                    BaChat cachedChat = new BaChat();
                    cachedChat.setSenderId(senderId);
                    cachedChat.setReceiverId(userId);
                    cachedChat.setMessages(messages);
                    cachedChat.setUpdatedAt(LocalDateTime.now());
                    unreadChats.add(cachedChat);
                }
            }
        }

        // 3. 构造前端需要的数据结构并发送
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "UNREAD_CHATS");
        payload.put("data", unreadChats);

        String json = objectMapper.writeValueAsString(payload);
        session.sendMessage(new TextMessage(json));
    }



    private String findUserIdBySession(WebSocketSession session) {
        return sessions.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getId().equals(session.getId()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }


}
