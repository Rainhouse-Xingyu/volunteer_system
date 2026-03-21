package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.entity.User;
import com.volunteer.entity.UserUpdate;
import com.volunteer.service.UserUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/audit")
public class UserUpdateController {

    @Autowired
    private UserUpdateService userUpdateService;

    /**
     * Get pending audits
     */
    @RequireRole("admin")
    @GetMapping("/list")
    public Result<IPage<UserUpdate>> getAuditList(@RequestParam(defaultValue = "1") int current,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Integer status) {
        Page<UserUpdate> page = new Page<>(current, size);
        LambdaQueryWrapper<UserUpdate> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(UserUpdate::getStatus, status);
        }
        queryWrapper.orderByDesc(UserUpdate::getCreateTime);
        return Result.success(userUpdateService.page(page, queryWrapper));
    }

    /**
     * Audit update request
     */
    @RequireRole("admin")
    @PostMapping("/{id}")
    public Result<Void> auditUpdate(@PathVariable Integer id,
                                    @RequestBody Map<String, Object> body,
                                    @RequestAttribute("currentUser") User admin) {
        Integer status = (Integer) body.get("status");
        String reason = (String) body.get("reason");
        
        userUpdateService.auditUpdate(id, admin.getUserId(), status, reason);
        return Result.success();
    }
}
