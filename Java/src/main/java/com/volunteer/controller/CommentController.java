package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.CommentService;
import com.volunteer.utils.JwtUtils;
import com.volunteer.vo.CommentVO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 查询某个活动的所有评价
     */
    @GetMapping("/activity/{activityId}")
    public Result<List<CommentVO>> getActivityComments(@PathVariable Integer activityId) {
        List<CommentVO> list = commentService.getCommentsByActivityId(activityId);
        return Result.success(list);
    }

    /**
     * 志愿者在个人中心查看自己的评价历史
     */
    @GetMapping("/volunteer/my-comments")
    public Result<List<CommentVO>> getMyComments(HttpServletRequest request) {
        // 从 Token 获取 userId
        String token = request.getHeader("Authorization");
        Claims claims = jwtUtils.parseToken(token);
        Integer userId = claims.get("userId", Integer.class);
        
        List<CommentVO> list = commentService.getMyComments(userId);
        return Result.success(list);
    }
}
