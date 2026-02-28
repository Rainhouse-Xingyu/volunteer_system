package com.volunteer.controller;

import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.entity.Evaluation;
import com.volunteer.entity.User;
import com.volunteer.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 发布评价
     * POST /evaluation/create
     */
    @PostMapping("/create")
    public Result<Void> createEvaluation(@RequestBody Evaluation evaluation, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        evaluationService.createEvaluation(evaluation, currentUser.getUserId());
        return Result.success();
    }
}
