package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.CommentReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentReportMapper extends BaseMapper<CommentReport> {

    @Select("SELECT cr.report_id as reportId, cr.comment_id as commentId, cr.reporter_id as reporterId, " +
            "cr.reason, cr.detail, cr.status, cr.create_time as createTime, " +
            "c.content as comment_content, u.nickname as volunteer_name " +
            "FROM comment_reports cr " +
            "LEFT JOIN comments c ON cr.comment_id = c.id " +
            "LEFT JOIN users u ON c.user_id = u.user_id " +
            "ORDER BY cr.report_id DESC")
    List<Map<String, Object>> selectAllReports();
}