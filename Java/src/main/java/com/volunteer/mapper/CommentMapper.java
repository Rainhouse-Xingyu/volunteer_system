package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Comment;
import com.volunteer.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论 Mapper 接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 查询活动的所有评论，关联用户表
     * @param activityId 活动ID
     * @return 评论VO列表
     */
    @Select("SELECT c.*, u.username " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.activity_id = #{activityId} " +
            "ORDER BY c.created_at DESC")
    List<CommentVO> selectCommentsByActivityId(@Param("activityId") Integer activityId);

    /**
     * 查询某用户的评论历史，关联活动表
     * @param userId 用户ID
     * @return 评论VO列表
     */
    @Select("SELECT c.*, a.title as activity_title " +
            "FROM comments c " +
            "LEFT JOIN activities a ON c.activity_id = a.activity_id " +
            "WHERE c.user_id = #{userId} " +
            "ORDER BY c.created_at DESC")
    List<CommentVO> selectCommentsByUserId(@Param("userId") Integer userId);
}
