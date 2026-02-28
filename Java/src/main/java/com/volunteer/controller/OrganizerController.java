package com.volunteer.controller;

import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.entity.OrganizerProfile;
import com.volunteer.entity.User;
import com.volunteer.service.OrganizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 组织者控制器
 */
@RestController
@RequestMapping("/organizer")
public class OrganizerController {

    @Autowired
    private OrganizerService organizerService;

    /**
     * 获取当前组织者资料
     */
    @RequireRole("organizer")
    @GetMapping("/me")
    public Result<OrganizerProfile> getProfile(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        return Result.success(organizerService.getProfile(currentUser.getUserId()));
    }

    /**
     * 更新组织者资料
     */
    @RequireRole("organizer")
    @PostMapping("/update")
    public Result<Void> updateProfile(@RequestBody OrganizerProfile profile, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        organizerService.updateProfile(profile, currentUser.getUserId());
        return Result.success();
    }
}
