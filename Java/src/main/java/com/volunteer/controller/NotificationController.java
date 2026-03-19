package com.volunteer.controller;

import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.entity.Notification;
import com.volunteer.service.NotificationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Notification;
import com.volunteer.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 消息通知控制器 (包含系统公告)
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 发布系统公告 (管理员)
     * POST /notifications/announce
     */
    @RequireRole("admin")
    @PostMapping("/announce")
    public Result<String> announce(@RequestBody Map<String, String> param) {
        String title = param.get("title");
        String content = param.get("content");
        if (title == null || content == null) {
            return Result.error(400, "标题和内容不能为空");
        }
        notificationService.sendNotice(0, title, content, "system_msg");
        return Result.success("发送成功");
    }

    /**
     * 获取我的消息列表
     * GET /notifications/my
     */
    @GetMapping("/my")
    public Result<IPage<Notification>> getMyNotifications(@RequestParam(defaultValue = "1") int current,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        Page<Notification> page = new Page<>(current, size);
        return Result.success(notificationService.getMyNotifications(page, currentUser.getUserId()));
    }

    /**
     * 标记消息为已读
     * POST /notifications/read/{id}
     */
    @PostMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Integer id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        notificationService.markAsRead(id, currentUser.getUserId());
        return Result.success();
    }
}
