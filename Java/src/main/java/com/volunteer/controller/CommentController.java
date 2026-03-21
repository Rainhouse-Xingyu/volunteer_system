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
     * 发表评价
     */
    @PostMapping("/add")
    public Result<String> addComment(@RequestBody CommentVO commentVO, HttpServletRequest request) {
        // 从 Token 获取 userId
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Claims claims = jwtUtils.parseToken(token);
        Integer userId = claims.get("userId", Integer.class);
        
        if (commentVO.getNewsId() != null) {
            commentService.postNewsComment(userId, commentVO.getNewsId(), commentVO.getContent());
        } else if (commentVO.getActivityId() != null) {
            commentService.postComment(userId, commentVO.getActivityId(), commentVO.getContent());
        } else {
            return Result.error("参数错误：未指定评论对象");
        }
        
        return Result.success("评价成功");
    }

    /**
     * 查询某个活动的所有评价
     */
    @GetMapping("/activity/{activityId}")
    public Result<List<CommentVO>> getActivityComments(@PathVariable Integer activityId) {
        List<CommentVO> list = commentService.getCommentsByActivityId(activityId);
        return Result.success(list);
    }

    /**
     * 查询资讯/故事的所有评价
     */
    @GetMapping("/news/{newsId}")
    public Result<List<CommentVO>> getNewsComments(@PathVariable Integer newsId) {
        List<CommentVO> list = commentService.getCommentsByNewsId(newsId);
        return Result.success(list);
    }

    /**
     * 志愿者在个人中心查看自己的评价历史
     */
    @GetMapping("/volunteer/my-comments")
    public Result<List<CommentVO>> getMyComments(HttpServletRequest request) {
        // 从 Token 获取 userId
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        Claims claims = jwtUtils.parseToken(token);
        Integer userId = claims.get("userId", Integer.class);
        
        List<CommentVO> list = commentService.getMyComments(userId);
        return Result.success(list);
    }
}
