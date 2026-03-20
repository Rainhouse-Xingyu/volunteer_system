package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Activity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 活动服务接口
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 创建活动
     * @param activity 活动信息
     * @param organizerId 组织者ID
     */
    void createActivity(Activity activity, Integer organizerId);

    /**
     * 分页查询已发布活动
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<Activity> getPublishedActivities(int current, int size);

    /**
     * 搜索活动 (增强筛选)
     * @param current 当前页
     * @param size 页大小
     * @param keyword 关键词(标题/内容/地点)
     * @param status 状态
     * @return 分页结果
     */
    IPage<Activity> searchActivities(int current, int size, String keyword, Integer status);

    /**
     * 获取活动详情 (带缓存)
     * @param activityId 活动ID
     * @return 活动详情
     */
    Activity getActivityDetail(Integer activityId);

    /**
     * 生成活动签到二维码Token
     * @param activityId 活动ID
     * @param organizerId 组织者ID (用于校验权限)
     * @return 签到Token
     */
    String generateSignToken(Integer activityId, Integer organizerId);


    /**
     * 获取组织者发布的活动
     * @param current 页码
     * @param size 页大小
     * @param organizerId 组织者ID
     * @param status 状态 (可选，逗号分割)
     * @return 分页结果
     */
    IPage<Activity> getMyCreatedActivities(int current, int size, Integer organizerId, String status);

    /**
     * 审核活动
     * @param activityId 活动ID
     * @param status 状态 (1:通过, 4:失败)
     */
    void auditActivity(Integer activityId, Integer status);
    
    /**
     * 获取组织者统计数据
     * @param organizerId 组织者ID
     * @return 统计Map
     */
    java.util.Map<String, Object> getOrganizerStats(Integer organizerId);
}

