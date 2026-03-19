package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.annotation.RequireRole;
import com.volunteer.common.Result;
import com.volunteer.entity.News;
import com.volunteer.entity.User;
import com.volunteer.service.NewsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 资讯控制器
 */
@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * 发布资讯
     */
    @PostMapping("/publish")
    @RequireRole("organizer")
    public Result<String> publish(@RequestBody News news, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        news.setOrganizerId(currentUser.getUserId());
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        news.setViews(0);
        
        // 默认为发布状态
        if (news.getStatus() == null) {
            news.setStatus(1);
        }
        
        boolean saved = newsService.save(news);
        return saved ? Result.success("发布成功") : Result.error(500, "发布失败");
    }

    /**
     * 公开资讯列表
     */
    @GetMapping("/list")
    public Result<Page<News>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        Page<News> page = new Page<>(current, size);
        LambdaQueryWrapper<News> query = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            query.like(News::getTitle, keyword);
        }
        
        query.eq(News::getStatus, 1); // 仅显示已发布
        query.orderByDesc(News::getCreatedAt);
        
        return Result.success(newsService.page(page, query));
    }
    
    /**
     * 主办方查询自己发布的资讯
     */
    @GetMapping("/my-list")
    @RequireRole("organizer")
    public Result<Page<News>> myList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) return Result.error(401, "未登录");

        Page<News> page = new Page<>(current, size);
        LambdaQueryWrapper<News> query = new LambdaQueryWrapper<>();
        query.eq(News::getOrganizerId, currentUser.getUserId());
        query.orderByDesc(News::getCreatedAt);
        
        return Result.success(newsService.page(page, query));
    }

    /**
     * 资讯详情
     */
    @GetMapping("/{id}")
    public Result<News> detail(@PathVariable Integer id) {
        News news = newsService.getById(id);
        if (news == null) {
            return Result.error(404, "资讯不存在");
        }
        
        // 增加阅读量
        news.setViews(news.getViews() + 1);
        newsService.updateById(news);
        
        return Result.success(news);
    }
    
    /**
     * 删除资讯
     */
    @PostMapping("/delete/{id}")
    @RequireRole("organizer")
    public Result<String> delete(@PathVariable Integer id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) return Result.error(401, "未登录");

        News news = newsService.getById(id);
        if (news == null) {
           return Result.error(404, "资讯不存在");
        }
        
        if (!news.getOrganizerId().equals(currentUser.getUserId())) {
            return Result.error(403, "无权删除");
        }
        
        newsService.removeById(id);
        return Result.success("删除成功");
    }
}
