package com.volunteer.controller;

import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import com.volunteer.service.UserService;
import com.volunteer.service.RegistrationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.User;
import com.volunteer.dto.RegistrationDTO;
import com.volunteer.dto.RegistrationExportDTO;
import com.alibaba.excel.EasyExcel;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private RegistrationService registrationService;


    /**
     * 获取用户列表（带分页和角色筛选）
     * GET /admin/users?current=1&size=10&role=volunteer
     */
    @RequireRole("admin")
    @GetMapping("/users")
    public Result<IPage<User>> getUserList(@RequestParam(defaultValue = "1") int current,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String role,
                                           @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(current, size);
        return Result.success(userService.getUserList(page, role, keyword));
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

    /**
     * 导出活动报名/签到报表
     */
    @RequireRole("admin")
    @GetMapping("/export/{activityId}")
    public void exportActivityReport(@PathVariable Integer activityId, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("报表" + activityId, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<RegistrationDTO> list = registrationService.getActivityRegistrations(activityId);
        List<RegistrationExportDTO> exportList = list.stream().map(dto -> {
            RegistrationExportDTO export = new RegistrationExportDTO();
            // 姓名
            export.setName(dto.getVolunteerName() != null ? dto.getVolunteerName() : "未知");
            
            // 签到状态
            export.setCheckInStatus(dto.getCheckinStatus() != null && dto.getCheckinStatus() == 1 ? "已签到" : "未签到");
            
            // 时长
            double duration = 0.0;
            if (dto.getStartTime() != null && dto.getEndTime() != null) {
                java.time.Duration d = java.time.Duration.between(dto.getStartTime(), dto.getEndTime());
                duration = d.toMinutes() / 60.0;
                // 保留一位小数
                duration = Math.round(duration * 10.0) / 10.0;
            }
            export.setDuration(duration);
            
            // 积分
            export.setPoints(dto.getRewardPoints() != null ? dto.getRewardPoints() : 0);
            return export;
        }).collect(Collectors.toList());

        EasyExcel.write(response.getOutputStream(), RegistrationExportDTO.class)
                .sheet("签到报表")
                .doWrite(exportList);
    }
}
