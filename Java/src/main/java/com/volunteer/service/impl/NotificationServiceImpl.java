package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Notification;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.NotificationMapper;
import com.volunteer.service.NotificationService;
import com.volunteer.websocket.WebSocketServer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 通知服务实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public void sendNotice(Integer receiverId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setSenderId(0); // 默认 0 (系统)
        notification.setReceiverId(receiverId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        
        this.baseMapper.insert(notification);
        
        // 推送 WebSocket 消息
        // 如果是全平台广播 (0)，则广播；否则单推
        if (receiverId == 0) {
            WebSocketServer.broadcast(content);
        } else {
            WebSocketServer.sendMessage(String.valueOf(receiverId), content);
        }
    }

    @Override
    public IPage<Notification> getMyNotifications(Page<Notification> page, Integer userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        // 查询 receiver_id = userId OR receiver_id = 0
        queryWrapper.and(wrapper -> 
            wrapper.eq(Notification::getReceiverId, userId)
                   .or()
                   .eq(Notification::getReceiverId, 0)
        );
        // 按时间倒序
        queryWrapper.orderByDesc(Notification::getCreatedAt);
        return this.page(page, queryWrapper);
    }

    @Override
    public void markAsRead(Integer noticeId, Integer userId) {
        Notification notification = this.getById(noticeId);
        if (notification == null) {
            throw new ServiceException("通知不存在");
        }
        
        // Check permissions (must be for this user OR global)
        if (!userId.equals(notification.getReceiverId()) && !Integer.valueOf(0).equals(notification.getReceiverId())) {
             throw new ServiceException("无权操作此通知");
        }
        
        // Only mark personal notifications as read
        if (Integer.valueOf(0).equals(notification.getReceiverId())) {
            return; 
        }

        notification.setIsRead(1);
        this.updateById(notification);
    }

    @Override
    public void markAllAsRead(Integer userId) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Notification::getIsRead, 1)
                     .eq(Notification::getReceiverId, userId)
                     .eq(Notification::getIsRead, 0);
        this.update(updateWrapper);
    }
}
