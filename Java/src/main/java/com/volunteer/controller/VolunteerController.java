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
                                             HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息异常");
        }
        
        return Result.success(registrationService.getMyRegistrations(current, size, currentUser.getUserId()));
    }

    /**
     * 扫码签到
     * @param param 包含 signToken
     */
    @PostMapping("/checkin")
    public Result<String> checkIn(@RequestBody java.util.Map<String, String> param, HttpServletRequest request) {
        String signToken = param.get("signToken");
        if (signToken == null || signToken.trim().isEmpty()) {
            return Result.error(400, "Token不能为空");
        }

        User currentUser = (User) request.getAttribute("currentUser");
        registrationService.checkIn(signToken, currentUser.getUserId());
        
        return Result.success("签到成功");
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
}
