package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.CommentReport;

import java.util.List;
import java.util.Map;

public interface CommentReportService extends IService<CommentReport> {
    /**
     * 提交举报
     */
    void submitReport(Integer userId, Integer commentId, String reason, String detail);

    List<Map<String, Object>> getAllReports();

    /**
     * 处理举报
     * @param reportId 举报ID
     * @param status 处理状态
     */
    void processReport(Integer reportId, Integer status);
}