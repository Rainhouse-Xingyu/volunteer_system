package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Registration;

/**
 * 报名服务接口
 */
public interface RegistrationService extends IService<Registration> {

    /**
     * 活动报名
     * @param activityId 活动ID
     * @param userId 用户ID
     */
    void register(Integer activityId, Integer userId);

    /**
     * 取消报名
     * @param activityId 活动ID
     * @param userId 用户ID
     */
    void cancelRegistration(Integer activityId, Integer userId);

    /**
     * 获取用户的报名列表 (分页)
     * @param current 页码
     * @param size 每页大小
     * @param userId 用户ID
     * @param status 状态 (0:待审核, 1:即将参加, 2:已完成, 3:已驳回, 4:已取消)
     * @return 报名列表
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.dto.RegistrationDTO> getMyRegistrations(int current, int size, Integer userId, Integer status);
    
    /**
     * 初始化/重置活动库存 (辅助方法，用于测试或数据同步)
     */
    void initActivityQuota(Integer activityId);

    /**
     * 志愿者扫码签到 (旧版: 仅通过Token反查)
     * @param signToken 二维码中的Token
     * @param userId 志愿者用户ID
     */
    void checkIn(String signToken, Integer userId);

    /**
     * 志愿者扫码签到 (新版: 指定活动ID校验)
     * @param activityId 活动ID
     * @param signToken 签到Token
     * @param userId 志愿者用户ID
     */
    void checkIn(Integer activityId, String signToken, Integer userId);

    /**
     * 获取用户在某活动的报名详情状态
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 报名DTO (含状态信息)，未报名返回 null
     */
    com.volunteer.dto.RegistrationDTO getRegistrationStatus(Integer activityId, Integer userId);

    /**
     * 获取活动的所有报名名单 (含志愿者信息)
     * @param activityId 活动ID
     * @return 报名列表
     */
    java.util.List<com.volunteer.dto.RegistrationDTO> getActivityRegistrations(Integer activityId);

    /**
     * 获取活动的所有报名名单 (分页)
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.dto.RegistrationDTO> listByActivity(Integer activityId, int current, int size);

    /**
     * 审批志愿者报名
     * @param registrationId 报名记录ID
     * @param targetStatus 目标状态 (1录取, 2拒绝)
     * @param organizerId 操作者ID (用于权限校验)
     */
    void auditRegistration(Integer registrationId, Integer targetStatus, Integer organizerId);

    /**
     * 生成并获取活动签到码 (仅组织者)
     * @param activityId 活动ID
     * @param organizerId 组织者ID
     * @return 签到Token
     */
    String generateCheckInCode(Integer activityId, Integer organizerId);
}




