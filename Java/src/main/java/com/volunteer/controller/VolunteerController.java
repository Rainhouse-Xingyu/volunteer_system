package com.volunteer.controller;

import com.volunteer.entity.User;
import com.volunteer.common.Result;
import com.volunteer.dto.RegistrationDTO;
import com.volunteer.entity.VolunteerProfile;
import com.volunteer.service.RegistrationService;
import com.volunteer.service.VolunteerService;
import com.volunteer.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import com.volunteer.entity.Notification;
import com.volunteer.service.NotificationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 志愿者控制器
 */
@RestController
@RequestMapping("/volunteer")
public class VolunteerController {

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private com.volunteer.service.UserService userService;

    @Autowired
    private com.volunteer.service.ActivityService activityService;

    /**
     * 获取志愿者仪表盘统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息异常");
        }

        // 1. 获取已参与活动数量 (状态为1:已录取)
        List<com.volunteer.entity.Registration> registrations = registrationService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.volunteer.entity.Registration>()
                .eq(com.volunteer.entity.Registration::getVolunteerId, currentUser.getUserId())
                .eq(com.volunteer.entity.Registration::getRegStatus, 1)); // 1:已录取

        long count = registrations.size();

        // Calculate volunteer hours based on checked-in activities
        long totalMinutes = 0;
        for (com.volunteer.entity.Registration reg : registrations) {
             // Only count if checked in
             if (reg.getCheckinStatus() != null && reg.getCheckinStatus() == 1) {
                 com.volunteer.entity.Activity activity = activityService.getById(reg.getActivityId());
                 if (activity != null && activity.getStartTime() != null && activity.getEndTime() != null) {
                     java.time.Duration duration = java.time.Duration.between(activity.getStartTime(), activity.getEndTime());
                     totalMinutes += duration.toMinutes();
                 }
             }
        }
        
        double hours = totalMinutes / 60.0;
        hours = Math.round(hours * 10.0) / 10.0;

        // 2. 获取最新积分和信誉分
        User user = userService.getById(currentUser.getUserId());
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("activityCount", count);
        stats.put("points", user.getPoints());
        stats.put("creditScore", user.getCreditScore());
        stats.put("volunteerHours", hours);
        
        return Result.success(stats);
    }

    /**
     * 获取当前登录志愿者资料
     * @param request HTTP请求
     * @return 志愿者资料
     */
    @GetMapping("/me")
    public Result<VolunteerProfile> getProfile(HttpServletRequest request) {
        // 从拦截器设置的属性中获取当前用户
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息异常");
        }

        VolunteerProfile profile = volunteerService.getProfile(currentUser.getUserId());
        return Result.success(profile);
    }

    /**
     * 完善/更新志愿者资料
     * @param volunteerProfile 志愿者资料信息
     * @param request HTTP请求
     * @return 结果
     */
    @PostMapping("/update")
    public Result<Void> updateProfile(@RequestBody VolunteerProfile volunteerProfile, 
                                      HttpServletRequest request) {
        // 从拦截器设置的属性中获取当前用户
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息异常");
        }

        // 调用 Service 更新资料
        volunteerService.updateProfile(volunteerProfile, currentUser.getUserId());

        return Result.success();
    }

    /**
     * 我的活动记录
     * GET /api/volunteer/my-registrations
     * 返回字段：活动标题、活动时间、我的报名状态、我的签到状态
     */
    @GetMapping("/my-registrations")
    public Result<Object> getMyRegistrations(@RequestParam(defaultValue = "1") int current,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) Integer status,
                                             HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息异常");
        }
        
        return Result.success(registrationService.getMyRegistrations(current, size, currentUser.getUserId(), status));
    }

    /**
     * 扫码签到
     * @param param 包含 activityId, signToken
     */
    @PostMapping("/checkin")
    public Result<String> checkIn(@RequestBody java.util.Map<String, Object> param, HttpServletRequest request) {
        String signToken = (String) param.get("signToken");
        Integer activityId = (Integer) param.get("activityId");
        
        if (signToken == null || signToken.trim().isEmpty()) {
            return Result.error(400, "Token不能为空");
        }
        if (activityId == null) {
            return Result.error(400, "活动ID不能为空");
        }

        User currentUser = (User) request.getAttribute("currentUser");
        registrationService.checkIn(activityId, signToken, currentUser.getUserId());
        
        return Result.success("签到成功");
    }

    /**
     * 完成活动
     */
    @PostMapping("/complete/{regId}")
    public Result<Void> completeActivity(@PathVariable Integer regId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
             return Result.error(401, "用户信息异常");
        }
        com.volunteer.entity.Registration registration = registrationService.getById(regId);
        if (registration == null) {
            return Result.error(404, "报名记录不存在");
        }
        if (!registration.getVolunteerId().equals(currentUser.getUserId())) {
            return Result.error(403, "无权操作");
        }
        registration.setRegStatus(2); // 2: Completed
        registrationService.updateById(registration);
        return Result.success();
    }


    /**
     * 查询某活动报名与签到状态
     * @param activityId 活动ID
     */
    @GetMapping("/activity-status/{activityId}")
    public Result<RegistrationDTO> getActivityStatus(@PathVariable Integer activityId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息异常");
        }
        
        RegistrationDTO dto = registrationService.getRegistrationStatus(activityId, currentUser.getUserId());
        // 如果 dto 为 null，说明未报名。具体返回逻辑视前端需求而定。
        // 这里返回 null data，前端根据 null 判定为“未报名”状态
        return Result.success(dto);
    }

    /**
     * 积分排行榜
     * GET /volunteer/leaderboard
     * 返回：Top 10 用户列表 + 当前用户排名
     */
    @GetMapping("/leaderboard")
    public Result<Map<String, Object>> getLeaderboard(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息异常");
        }
        
        return Result.success(volunteerService.getLeaderboard(currentUser.getUserId()));
    }

    /**
     * 获取推荐活动（不冲突的活动）
     */
    @GetMapping("/recommendation")
    public Result<com.volunteer.entity.Activity> getRecommendedActivity(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息异常");
        }
        
        return Result.success(volunteerService.getRecommendedActivity(currentUser.getUserId()));
    }

    /**
     * 获取我的通知列表
     * @param current 页码
     * @param size 每页数量
     */
    @GetMapping("/notifications")
    public Result<IPage<Notification>> getMyNotifications(@RequestParam(defaultValue = "1") int current,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        Page<Notification> page = new Page<>(current, size);
        IPage<Notification> result = notificationService.getMyNotifications(page, currentUser.getUserId());
        return Result.success(result);
    }

    /**
     * 标记通知为已读
     * @param noticeId 通知ID
     */
    @PutMapping("/notifications/{noticeId}/read")
    public Result<Void> markAsRead(@PathVariable Integer noticeId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        notificationService.markAsRead(noticeId, currentUser.getUserId());
        return Result.success();
    }

    /**
     * 标记全部已读
     */
    @PutMapping("/notifications/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        notificationService.markAllAsRead(currentUser.getUserId());
        return Result.success();
    }
}
