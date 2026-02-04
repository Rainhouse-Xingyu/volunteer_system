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
     * 获取我的报名列表
     * @param request HTTP请求
     * @return 报名列表
     */
    @GetMapping("/activities")
    public Result<List<RegistrationDTO>> getMyActivities(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "用户信息异常");
        }
        
        return Result.success(registrationService.getMyRegistrations(currentUser.getUserId()));
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
}
