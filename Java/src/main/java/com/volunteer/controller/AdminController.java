package com.volunteer.controller;

import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import com.volunteer.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import com.volunteer.entity.User;

import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（带分页和角色筛选）
     * GET /admin/users?current=1&size=10&role=volunteer
     */
    @RequireRole("admin")
    @GetMapping("/users")
    public Result<IPage<User>> getUserList(@RequestParam(defaultValue = "1") int current,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String role) {
        Page<User> page = new Page<>(current, size);
        return Result.success(userService.getUserList(page, role));
    }

    /**
     * 更新用户状态（禁用/启用）
     * PUT /admin/user/status
     */
    @RequireRole("admin")
    @PutMapping("/user/status")
    public Result<Void> updateUserStatus(@RequestBody Map<String, Object> param) {
        if (!param.containsKey("userId") || !param.containsKey("status")) {
            return Result.error(400, "参数缺失");
        }

        Integer userId = Integer.valueOf(param.get("userId").toString());
        Integer status = Integer.valueOf(param.get("status").toString());

        userService.updateUserStatus(userId, status);
        return Result.success();
    }

    /**
     * 活动审核
     * PUT /admin/activity/audit
     * @param param { "activityId": 123, "result": 1 }
     */
    @RequireRole("admin")
    @CacheEvict(value = "activities", allEntries = true)
    @PutMapping("/activity/audit")
    public Result<Void> auditActivity(@RequestBody Map<String, Object> param) {
        if (!param.containsKey("activityId") || !param.containsKey("result")) {
            return Result.error(400, "参数缺失");
        }

        Integer activityId = Integer.valueOf(param.get("activityId").toString());
        Integer result = Integer.valueOf(param.get("result").toString());

        // 1通过, 4审核失败
        if (result != 1 && result != 4) {
            return Result.error(400, "非法的审核状态");
        }

        // 业务逻辑交由 Service 处理（包括状态更新、Redis名额初始化等）
        activityService.auditActivity(activityId, result);
        return Result.success();
    }
}
