package com.volunteer.controller;

import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.User;
import com.volunteer.service.ActivityService;
import com.volunteer.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private RegistrationService registrationService;

    /**
     * 发布活动
     * @param activity 活动信息
     * @param request 请求对象，用于获取当前登录用户
     */
    @RequireRole("organizer")
    @PostMapping("/create")
    public Result<Void> createActivity(@RequestBody Activity activity, HttpServletRequest request) {
        // 从 Request 中获取当前登录用户
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息缺失，请重新登录");
        }
        
        activityService.createActivity(activity, currentUser.getUserId());
        return Result.success();
    }

    /**
     * 活动报名 (秒杀抢单)
     * @param activityId 活动ID
     * @param request 请求对象
     */
    @RequireRole("volunteer")
    @PostMapping("/register/{activityId}")
    public Result<Void> register(@PathVariable Integer activityId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        registrationService.register(activityId, currentUser.getUserId());
        return Result.success();
    }

    /**
     * 取消报名 (库存回滚)
     * @param activityId 活动ID
     * @param request 请求对象
     */
    @RequireRole("volunteer")
    @PostMapping("/cancel/{activityId}")
    public Result<Void> cancel(@PathVariable Integer activityId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        registrationService.cancelRegistration(activityId, currentUser.getUserId());
        return Result.success();
    }

    /**
     * 生成动态签到码
     * @param activityId 活动ID
     */
    @RequireRole("organizer")
    @GetMapping("/sign-token/{activityId}")
    public Result<String> getSignToken(@PathVariable Integer activityId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        String token = activityService.generateSignToken(activityId, currentUser.getUserId());
        return Result.success(token);
    }

    /**
     * 志愿者被动扫码签到 (新版)
     * @param param 包含 activityId 和 signToken
     */
    @RequireRole("volunteer")
    @PostMapping("/checkin")
    public Result<String> checkIn(@RequestBody java.util.Map<String, Object> param, HttpServletRequest request) {
        if (!param.containsKey("activityId") || !param.containsKey("signToken")) {
            return Result.error(400, "参数不完整");
        }
        
        Integer activityId = Integer.valueOf(param.get("activityId").toString());
        String signToken = param.get("signToken").toString();
        
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        registrationService.checkIn(activityId, signToken, currentUser.getUserId());
        return Result.success("签到成功");
    }

    /**
     * 查看活动报名名单
     * @param activityId 活动ID
     */
    @RequireRole("organizer")
    @GetMapping("/registrations/{activityId}")
    public Result<java.util.List<com.volunteer.dto.RegistrationDTO>> getRegistrations(@PathVariable Integer activityId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        // 校验活动权限
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }
        if (!activity.getOrganizerId().equals(currentUser.getUserId())) {
            return Result.error(403, "您无权查看该活动的报名名单");
        }
        
        return Result.success(registrationService.getActivityRegistrations(activityId));
    }

    /**
     * 审批志愿者报名
     * @param param 包含 registrationId 和 targetStatus
     */
    @RequireRole("organizer")
    @PutMapping("/registration/audit")
    public Result<Void> auditRegistration(@RequestBody java.util.Map<String, Integer> param, HttpServletRequest request) {
        if (!param.containsKey("registrationId") || !param.containsKey("targetStatus")) {
            return Result.error(400, "参数缺失");
        }

        Integer registrationId = param.get("registrationId");
        Integer targetStatus = param.get("targetStatus");
        
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        registrationService.auditRegistration(registrationId, targetStatus, currentUser.getUserId());
        return Result.success();
    }

    /**
     * 分页查询已发布活动 (支持关键词搜索和状态筛选)
     * GET /activity/list?current=1&size=10&keyword=义工&status=1
     */
    @GetMapping("/list")
    public Result<IPage<com.volunteer.entity.Activity>> listActivities(
            @RequestParam(defaultValue = "1") int current, 
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        if (keyword == null && status == null) {
            // 默认查询
            return Result.success(activityService.getPublishedActivities(current, size));
        } else {
            // 搜索查询
            return Result.success(activityService.searchActivities(current, size, keyword, status));
        }
    }

    /**
     * 查看活动详情
     * @param id 活动ID
     */
    @GetMapping("/{id}")
    public Result<Activity> getActivity(@PathVariable Integer id) {
        return Result.success(activityService.getActivityDetail(id));
    }

    /**
     * 获取我发布的活动
     */
    @RequireRole("organizer")
    @GetMapping("/my-list")
    public Result<IPage<Activity>> getMyActivities(
        @RequestParam(defaultValue = "1") int current,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest request) {
        
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        return Result.success(activityService.getMyCreatedActivities(current, size, currentUser.getUserId()));
    }
}
