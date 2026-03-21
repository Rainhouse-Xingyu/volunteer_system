package com.volunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.CommentReport;
import com.volunteer.mapper.CommentReportMapper;
import com.volunteer.service.CommentReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.volunteer.entity.Comment;
import com.volunteer.entity.User;
import com.volunteer.mapper.CommentMapper;
import com.volunteer.mapper.UserMapper;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentReportServiceImpl extends ServiceImpl<CommentReportMapper, CommentReport> implements CommentReportService {

    @Autowired
    private CommentReportMapper reportMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void submitReport(Integer userId, Integer commentId, String reason, String detail) {
        CommentReport report = new CommentReport();
        report.setCommentId(commentId);
        report.setReporterId(userId);
        report.setReason(reason);
        report.setDetail(detail);
        report.setStatus(0); // 待处理
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        this.save(report);
    }

    @Override
    public List<Map<String, Object>> getAllReports() {
        return reportMapper.selectAllReports();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processReport(Integer reportId, Integer status) {
        CommentReport report = this.getById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }
        
        // 更新状态
        report.setStatus(status);
        report.setUpdateTime(LocalDateTime.now());
        this.updateById(report);

        // 如果确认为违规 (status == 1)，扣除积分
        if (status == 1) {
            Comment comment = commentMapper.selectById(report.getCommentId());
            if (comment != null) {
                User user = userMapper.selectById(comment.getUserId());
                if (user != null && "VOLUNTEER".equals(user.getRole())) {
                    // 扣除1分信誉分，最低不低于0
                    int currentScore = user.getCreditScore() != null ? user.getCreditScore() : 100;
                    if (currentScore > 0) {
                        user.setCreditScore(currentScore - 1);
                        userMapper.updateById(user);
                    }
                }
                
                // 同时可以将评论状态改为2 (违规隐藏)
                comment.setStatus(2);
                commentMapper.updateById(comment);
            }
        }
    }
}