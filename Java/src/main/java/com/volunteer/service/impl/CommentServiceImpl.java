package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Comment;
import com.volunteer.entity.Registration;
import com.volunteer.entity.User;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.CommentMapper;
import com.volunteer.mapper.RegistrationMapper;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.CommentService;
import com.volunteer.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评论服务实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postComment(Integer userId, Integer activityId, String content) {
        // 1. 检查该用户是否参加了此活动 (且已录取)
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getVolunteerId, userId)
                  .eq(Registration::getActivityId, activityId);
        
        Registration registration = registrationMapper.selectOne(regWrapper);
        if (registration == null) {
            throw new ServiceException("您未报名参加该活动");
        }

        // 2. 检查 checkin_status 是否为 1（已签到）
        if (registration.getCheckinStatus() == null || registration.getCheckinStatus() != 1) {
            throw new ServiceException("您尚未签到，无法发表评论");
        }

        // 3. 检查该用户是否已经对该活动发表过评论（防止重复评价）
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getUserId, userId)
                      .eq(Comment::getTargetId, activityId)
                      .eq(Comment::getTargetType, "activity");
        
        if (this.count(commentWrapper) > 0) {
            throw new ServiceException("您已经评价过该活动，请勿重复评价");
        }

        // 4. 插入评价
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetId(activityId);
        comment.setTargetType("activity");
        comment.setContent(content);
        // createdAt 使用 SQL 默认时间
        
        this.save(comment);

        // 5. 联动逻辑：给该用户增加 2 点积分
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPoints((user.getPoints() == null ? 0 : user.getPoints()) + 2);
            userMapper.updateById(user);
        }
    }

    @Override
    public List<CommentVO> getCommentsByActivityId(Integer activityId) {
        return baseMapper.selectCommentsByActivityId(activityId);
    }

    @Override
    public void postNewsComment(Integer userId, Integer newsId, String content) {
        // Step 1: Check if user already commented? (Optional, maybe allow multiple comments on news)
        // For simplicity allow multiple.

        // Step 2: Save comment
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetId(newsId);
        comment.setTargetType("story");
        comment.setContent(content);
        // comment.setCreatedAt handled by DB default or set here
        comment.setCreatedAt(java.time.LocalDateTime.now());
        
        this.save(comment);

        // Step 3: Maybe update News.commentsCount if needed (Entity News doesn't have it apparently)
    }

    @Override
    public List<CommentVO> getCommentsByNewsId(Integer newsId) {
        return baseMapper.selectCommentsByNewsId(newsId);
    }

    @Override
    public List<CommentVO> getMyComments(Integer userId) {
        return baseMapper.selectCommentsByUserId(userId);
    }
}
