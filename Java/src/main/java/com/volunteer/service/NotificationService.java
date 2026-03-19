package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Notification;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 发送通知
     * @param receiverId 接收者ID
     * @param title 标题
     * @param content 内容
     * @param type 类型
     */
    void sendNotice(Integer receiverId, String title, String content, String type);

    /**
     * 获取用户通知（包括个人通知和全站广播）
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<Notification> getMyNotifications(Page<Notification> page, Integer userId);

    /**
     * 标记通知为已读
     * @param noticeId 通知ID
     * @param userId 操作用户ID (用于安全校验)
     */
    void markAsRead(Integer noticeId, Integer userId);

    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     */
    void markAllAsRead(Integer userId);
}
