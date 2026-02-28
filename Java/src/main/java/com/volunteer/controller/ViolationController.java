package com.volunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.entity.User;
import com.volunteer.entity.Violation;
import com.volunteer.service.ViolationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 违规处理控制器
 */
@RestController
@RequestMapping("/violation")
public class ViolationController {

    @Autowired
    private ViolationService violationService;

    /**
     * 提交举报 (志愿者/组织者)
     * POST /violation/report
     */
    @PostMapping("/report")
    public Result<Void> report(@RequestBody Violation violation, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        violationService.reportViolation(violation, currentUser.getUserId());
        return Result.success();
    }

    /**
     * 获取违规列表 (管理员)
     * GET /violation/list
     */
    @RequireRole("admin")
    @GetMapping("/list")
    public Result<IPage<Violation>> list(@RequestParam(defaultValue = "1") int current,
                                         @RequestParam(defaultValue = "10") int size) {
        Page<Violation> page = new Page<>(current, size);
        return Result.success(violationService.getViolationList(page));
    }

    /**
     * 处理违规 (管理员)
     * PUT /violation/process
     */
    @RequireRole("admin")
    @PutMapping("/process")
    public Result<Void> process(@RequestBody Map<String, Object> param, HttpServletRequest request) {
        if (!param.containsKey("violationId") || !param.containsKey("reason")) {
            return Result.error(400, "参数缺失");
        }

        Integer violationId = Integer.valueOf(param.get("violationId").toString());
        String reason = param.get("reason").toString();
        
        User currentUser = (User) request.getAttribute("currentUser");
        
        violationService.processViolation(violationId, reason, currentUser.getUserId());
        return Result.success();
    }
}
