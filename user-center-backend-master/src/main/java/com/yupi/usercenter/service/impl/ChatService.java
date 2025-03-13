package com.yupi.usercenter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/{username}")
public class ChatService {
    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();
    private String username;

    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) throws JsonProcessingException, JsonProcessingException {
        this.username = username;
        SESSIONS.put(username, session);

        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        String time = String.format("%02d:%02d", hour, minute);
        System.out.println(time);

        Map<String,Object> map = new HashMap<>();
        map.put("username","索隆");
        map.put("text","您吃了吗？");
        map.put("time",time);
        // 创建 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = objectMapper.writeValueAsString(map);
        broadcast(jsonString);
        System.out.println("ws连接成功");
    }


    @Scheduled(cron = "0/30 * * * * ?")
    public void pushBy3s() throws JsonProcessingException {

        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        String time = String.format("%02d:%02d", hour, minute);
        Map<String,Object> map = new HashMap<>();
        map.put("username","索隆");
        map.put("text","我吃了，您呢？");
        map.put("time",time);
        // 创建 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = objectMapper.writeValueAsString(map);
        broadcast(jsonString);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {

        broadcast(message);
        System.out.println("ws收到消息: "+message);
    }

    @OnClose
    public void onClose() {
        SESSIONS.remove(username);
        broadcast("🔴 " + username + " 离开了聊天室！");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket 连接出错：" + throwable.getMessage());
    }

    private void broadcast(String message) {
        SESSIONS.values().forEach(session -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
