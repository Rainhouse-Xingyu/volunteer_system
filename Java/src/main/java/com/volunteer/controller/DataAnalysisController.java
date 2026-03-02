package com.volunteer.controller;

import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.entity.User;
import com.volunteer.service.DataAnalysisService;
import com.volunteer.vo.ChartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analysis")
public class DataAnalysisController {

    @Autowired
    private DataAnalysisService dataAnalysisService;

    /**
     * 导出活动报名名单 (Excel)
     * GET /analysis/export/registrations/1
     */
    @RequireRole("organizer")
    @GetMapping("/export/registrations/{activityId}")
    public void exportRegistrations(@PathVariable Integer activityId, 
                                    HttpServletResponse response, 
                                    HttpServletRequest request) throws IOException {
        User currentUser = (User) request.getAttribute("currentUser");
        dataAnalysisService.exportRegistrations(response, activityId, currentUser.getUserId());
    }

    /**
     * 获取活动状态分布 (饼图)
     * GET /analysis/chart/activity-status
     */
    @GetMapping("/chart/activity-status")
    public Result<List<ChartVO>> getActivityStatusChart() {
        return Result.success(dataAnalysisService.getActivityStatusDistribution());
    }

    /**
     * 获取用户增长趋势 (折线图)
     * GET /analysis/chart/user-growth
     */
    @GetMapping("/chart/user-growth")
    public Result<Map<String, Object>> getUserGrowthChart() {
        return Result.success(dataAnalysisService.getUserGrowthTrend());
    }
}
