package com.volunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.OrganizerProfile;
import com.volunteer.entity.User;
import com.volunteer.mapper.OrganizerProfileMapper;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.OrganizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrganizerServiceImpl extends ServiceImpl<OrganizerProfileMapper, OrganizerProfile> implements OrganizerService {

    @Autowired
    private UserMapper userMapper;

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
        
        // 1. 更新 User 表 (昵称、头像)
        if (StringUtils.hasText(profile.getNickname()) || StringUtils.hasText(profile.getAvatarUrl())) {
            User user = new User();
            user.setUserId(userId);
            if (StringUtils.hasText(profile.getNickname())) {
                user.setNickname(profile.getNickname());
            }
            if (StringUtils.hasText(profile.getAvatarUrl())) {
                user.setAvatarUrl(profile.getAvatarUrl());
            }
            userMapper.updateById(user);
        }
        
        // 2. 更新 OrganizerProfile 表
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
