package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteer.entity.OrganizerProfile;
import com.volunteer.entity.UserUpdate;
import com.volunteer.entity.VolunteerProfile;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.UserUpdateMapper;
import com.volunteer.service.OrganizerService;
import com.volunteer.service.UserUpdateService;
import com.volunteer.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import com.volunteer.entity.User;
import com.volunteer.mapper.UserMapper;
import org.springframework.util.StringUtils;

@Service
public class UserUpdateServiceImpl extends ServiceImpl<UserUpdateMapper, UserUpdate> implements UserUpdateService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private VolunteerService volunteerService;

    @Autowired
    @Lazy
    private OrganizerService organizerService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void submitUpdate(Integer userId, String type, Object original, Object modified) {
        UserUpdate update = new UserUpdate();
        update.setUserId(userId);
        update.setType(type);
        update.setStatus(0); // Works as "pending"
        update.setCreateTime(LocalDateTime.now());
        
        try {
            update.setOriginalData(objectMapper.convertValue(original, Map.class));
            update.setModifiedData(objectMapper.convertValue(modified, Map.class));
        } catch (Exception e) {
            throw new ServiceException("数据转换失败");
        }

        this.save(update);
    }

    @Override
    @Transactional
    public void auditUpdate(Integer id, Integer adminId, Integer status, String reason) {
        if (status != 1 && status != 2) {
            throw new ServiceException("审核状态无效");
        }

        UserUpdate update = this.getById(id);
        if (update == null) {
            throw new ServiceException("审核记录不存在");
        }
        if (update.getStatus() != 0) {
            throw new ServiceException("该记录已审核");
        }

        update.setStatus(status);
        update.setAdminId(adminId);
        update.setAuditReason(reason);
        update.setUpdateTime(LocalDateTime.now());

        if (status == 1) {
            // Apply changes
            applyUpdate(update);
        }

        this.updateById(update);
    }

    private void applyUpdate(UserUpdate update) {
        try {
            if ("volunteer_profile".equals(update.getType())) {
                VolunteerProfile profile = objectMapper.convertValue(update.getModifiedData(), VolunteerProfile.class);
                profile.setUserId(update.getUserId());
                
                // Update User table specific fields (nickname, avatar)
                updateUserInfo(update.getUserId(), profile.getNickname(), profile.getAvatarUrl());
                
                volunteerService.updateById(profile);
            } else if ("organizer_profile".equals(update.getType())) {
                OrganizerProfile profile = objectMapper.convertValue(update.getModifiedData(), OrganizerProfile.class);
                profile.setUserId(update.getUserId());
                
                // Update User table specific fields
                updateUserInfo(update.getUserId(), profile.getNickname(), profile.getAvatarUrl());
                
                organizerService.saveOrUpdate(profile);
            }
        } catch (Exception e) {
            throw new ServiceException("应用更新失败: " + e.getMessage());
        }
    }

    private void updateUserInfo(Integer userId, String nickname, String avatarUrl) {
        if (StringUtils.hasText(nickname) || StringUtils.hasText(avatarUrl)) {
            User user = new User();
            user.setUserId(userId);
            if (StringUtils.hasText(nickname)) user.setNickname(nickname);
            if (StringUtils.hasText(avatarUrl)) user.setAvatarUrl(avatarUrl);
            userMapper.updateById(user);
        }
    }
}
