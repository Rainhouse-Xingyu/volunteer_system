package com.volunteer.controller;

import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.entity.Notification;
import com.volunteer.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<Void> announce(@RequestBody Map<String, String> param) {
        String title = param.get("title");
        String content = param.get("content");
        
        if (title == null || content == null) {
            return Result.error(400, "标题和内容不能为空");
        }

        // 发送给接收者ID=0 (全平台)
        notificationService.sendNotice(0, title, content, "system_msg");
        return Result.success();
    }
}
