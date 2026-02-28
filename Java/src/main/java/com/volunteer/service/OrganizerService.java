package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.OrganizerProfile;

public interface OrganizerService extends IService<OrganizerProfile> {
    /**
     * 获取组织者资料
     */
    OrganizerProfile getProfile(Integer userId);

    /**
     * 更新组织者资料
     */
    void updateProfile(OrganizerProfile profile, Integer userId);
}
