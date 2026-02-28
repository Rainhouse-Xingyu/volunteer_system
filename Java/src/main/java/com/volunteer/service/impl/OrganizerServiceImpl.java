package com.volunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.OrganizerProfile;
import com.volunteer.mapper.OrganizerProfileMapper;
import com.volunteer.service.OrganizerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizerServiceImpl extends ServiceImpl<OrganizerProfileMapper, OrganizerProfile> implements OrganizerService {

    @Override
    public OrganizerProfile getProfile(Integer userId) {
        return this.getById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(OrganizerProfile profile, Integer userId) {
        // 确保 ID 一致
        profile.setUserId(userId);
        
        OrganizerProfile exist = this.getById(userId);
        if (exist == null) {
            // 如果不存在则新增
            this.save(profile);
        } else {
            // 存在则更新
            this.updateById(profile);
        }
    }
}
