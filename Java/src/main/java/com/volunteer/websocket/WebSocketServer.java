package com.volunteer.websocket;

import org.springframework.stereotype.Component;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端
 * 客户端连接地址: ws://localhost:8080/ws/notice/{userId}
 */
@ServerEndpoint("/ws/notice/{userId}")
@Component
public class WebSocketServer {

    // 存储在线连接: userId -> Session
    private static ConcurrentHashMap<String, Session> outputSessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        outputSessionMap.put(userId, session);
        System.out.println("用户连接: " + userId);
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        outputSessionMap.remove(userId);
        System.out.println("用户断开: " + userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /**
     * 发送消息给指定用户
     */
    public static void sendMessage(String userId, String message) {
        Session session = outputSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 广播消息
     */
    public static void broadcast(String message) {
        outputSessionMap.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
