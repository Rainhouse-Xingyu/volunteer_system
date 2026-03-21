package com.volunteer.service.impl;

import com.volunteer.service.UserUpdateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.OrganizerProfile;
import com.volunteer.entity.User;
import com.volunteer.mapper.OrganizerProfileMapper;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.OrganizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrganizerServiceImpl extends ServiceImpl<OrganizerProfileMapper, OrganizerProfile> implements OrganizerService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Lazy
    private UserUpdateService userUpdateService;

    @Override
    public OrganizerProfile getProfile(Integer userId) {
        OrganizerProfile profile = this.getById(userId);
        if (profile == null) {
            profile = new OrganizerProfile();
            profile.setUserId(userId);
        }
        
        // 补充 User 表字段
        User user = userMapper.selectById(userId);
        if (user != null) {
            profile.setNickname(user.getNickname());
            profile.setAvatarUrl(user.getAvatarUrl());
        }
        
        return profile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(OrganizerProfile profile, Integer userId) {
        // 确保 ID 一致
        profile.setUserId(userId);
        
        // 2. 提交审核
        OrganizerProfile original = this.getById(userId);
        if (original == null) {
            original = new OrganizerProfile();
            original.setUserId(userId);
        }
        
        User user = userMapper.selectById(userId);
        if (user != null) {
            original.setNickname(user.getNickname());
            original.setAvatarUrl(user.getAvatarUrl());
        }
        
        userUpdateService.submitUpdate(userId, "organizer_profile", original, profile);
    }
}
