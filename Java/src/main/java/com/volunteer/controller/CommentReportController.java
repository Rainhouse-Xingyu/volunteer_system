package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.CommentReportService;
import com.volunteer.entity.CommentReport;
import com.volunteer.utils.JwtUtils;
import com.volunteer.vo.ReportVO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentReportController {

    @Autowired
    private CommentReportService reportService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/report")
    public Result<String> reportComment(@RequestBody ReportVO reportVO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Claims claims = jwtUtils.parseToken(token);
        Integer userId = claims.get("userId", Integer.class);

        reportService.submitReport(userId, reportVO.getCommentId(), reportVO.getReason(), reportVO.getDetail());
        return Result.success("举报成功，等待审核");
    }

    @GetMapping("/reports")
    public Result<List<Map<String, Object>>> getReports() {
        // Can add role check here if needed (e.g., only Admin)
        return Result.success(reportService.getAllReports());
    }

    @PutMapping("/reports/{reportId}/process")
    public Result<String> processReport(@PathVariable Integer reportId, @RequestParam Integer status) {
        try {
            reportService.processReport(reportId, status);
            return Result.success("处理完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}