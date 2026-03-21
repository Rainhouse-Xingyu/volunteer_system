package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Comment;
import com.volunteer.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {
    
    /**
     * 发表评论
     * @param userId 用户ID
     * @param activityId 活动ID
     * @param content 评论内容
     */
    void postComment(Integer userId, Integer activityId, String content);

    /**
     * 发表资讯/故事评论
     */
    void postNewsComment(Integer userId, Integer newsId, String content);

    /**
     * 获取活动的所有评论
     * @param activityId 活动ID
     * @return 评论列表
     */
    List<CommentVO> getCommentsByActivityId(Integer activityId);

    /**
     * 获取资讯的所有评论
     */
    List<CommentVO> getCommentsByNewsId(Integer newsId);

    /**
     * 获取志愿者的评论历史
     * @param userId 用户ID
     * @return 评论列表
     */
    List<CommentVO> getMyComments(Integer userId);
}
