package com.volunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.dto.RegistrationDTO;
import com.volunteer.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 报名管理控制器
 */
@RestController
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 获取某活动报名列表（组织者）
     */
    @RequireRole("organizer")
    @GetMapping("/list/{activityId}")
    public Result<IPage<RegistrationDTO>> getRegistrationList(@PathVariable Integer activityId,
                                                              @RequestParam(defaultValue = "1") int current,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              HttpServletRequest request) {
        return Result.success(registrationService.listByActivity(activityId, current, size));
    }

    /**
     * 审核报名 (录用/拒绝)
     * POST /registration/audit
     * { "id": 1, "status": 1 }  // 1:录用, 2:拒绝
     */
    @RequireRole("organizer")
    @PostMapping("/audit")
    public Result<Void> auditRegistration(@RequestBody Map<String, Object> param, HttpServletRequest request) {
        if (!param.containsKey("id") || !param.containsKey("status")) {
            return Result.error(400, "参数缺失");
        }
        Integer regId = (Integer) param.get("id");
        Integer status = (Integer) param.get("status");
        
        // 获取组织者ID以进行权限校验
        com.volunteer.entity.User currentUser = (com.volunteer.entity.User) request.getAttribute("currentUser");
        
        registrationService.auditRegistration(regId, status, currentUser.getUserId());
        return Result.success();
    }

    /**
     * 生成活动签到码 (组织者)
     * POST /registration/checkin-code/{activityId}
     */
    @RequireRole("organizer")
    @PostMapping("/checkin-code/{activityId}")
    public Result<String> getCheckInCode(@PathVariable Integer activityId, HttpServletRequest request) {
        com.volunteer.entity.User currentUser = (com.volunteer.entity.User) request.getAttribute("currentUser");
        String code = registrationService.generateCheckInCode(activityId, currentUser.getUserId());
        return Result.success(code);
    }
}
