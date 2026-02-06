package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Notification;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.NotificationMapper;
import com.volunteer.service.NotificationService;
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
        
        if (!notification.getReceiverId().equals(userId) && !notification.getReceiverId().equals(0)) {
             throw new ServiceException("无权操作此通知");
        }
        
        // 简单处理：全平台通知暂不支持单独标记已读，或根据实际业务扩展
        if (notification.getReceiverId().equals(0)) {
            return; 
        }

        notification.setIsRead(1);
        this.updateById(notification);
    }
}
