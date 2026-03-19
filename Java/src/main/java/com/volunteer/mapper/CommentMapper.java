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
     * @param targetId 活动ID
     * @return 评论VO列表
     */
    @Select("SELECT c.id as comment_id, c.user_id, c.target_id as activity_id, c.content, c.created_at, " +
            "u.username, u.nickname as nickname, u.avatar_url as avatar " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.target_id = #{targetId} AND c.target_type = 'activity' " +
            "ORDER BY c.created_at DESC")
    List<CommentVO> selectCommentsByActivityId(@Param("targetId") Integer targetId);

    /**
     * 查询某用户的评论历史，关联活动表
     * @param userId 用户ID
     * @return 评论VO列表
     */
    @Select("SELECT c.id as comment_id, c.user_id, c.target_id as activity_id, c.content, c.created_at, " +
            "a.title as activity_title " +
            "FROM comments c " +
            "LEFT JOIN activities a ON c.target_id = a.activity_id AND c.target_type = 'activity' " +
            "WHERE c.user_id = #{userId} AND c.target_type = 'activity' " +
            "ORDER BY c.created_at DESC")
    List<CommentVO> selectCommentsByUserId(@Param("userId") Integer userId);
}
