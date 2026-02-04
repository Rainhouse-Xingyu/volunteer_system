package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.VolunteerProfile;

/**
 * 志愿者服务接口
 */
public interface VolunteerService extends IService<VolunteerProfile> {

    /**
     * 完善/更新志愿者资料
     * @param volunteerProfile 志愿者资料信息
     * @param userId 当前用户ID
     */
    void updateProfile(VolunteerProfile volunteerProfile, Integer userId);

    /**
     * 获取志愿者资料
     * @param userId 用户ID
     * @return 志愿者资料
     */
    VolunteerProfile getProfile(Integer userId);
}
