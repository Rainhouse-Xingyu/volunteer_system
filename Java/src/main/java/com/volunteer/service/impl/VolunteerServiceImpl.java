package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.VolunteerProfile;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.VolunteerProfileMapper;
import com.volunteer.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 志愿者服务实现类
 */
@Service
public class VolunteerServiceImpl extends ServiceImpl<VolunteerProfileMapper, VolunteerProfile> implements VolunteerService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 手机号正则校验
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    
    // Redis Key 前缀
    private static final String USER_PROFILE_KEY_PREFIX = "user:profile:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(VolunteerProfile volunteerProfile, Integer userId) {
        // 1. 设置用户ID
        volunteerProfile.setUserId(userId);

        // 安全处理：防止用户通过接口修改积分和信用评分
        // 将这两个字段置为 null，利用 MyBatis-Plus 默认的非空更新策略，避免更新这两个字段
        volunteerProfile.setPoints(null);
        volunteerProfile.setCreditScore(null);

        // 2. 校验手机号格式
        if (StringUtils.hasText(volunteerProfile.getPhone())) {
            if (!Pattern.matches(PHONE_REGEX, volunteerProfile.getPhone())) {
                throw new ServiceException("手机号格式不正确");
            }
        } else {
            throw new ServiceException("手机号不能为空");
        }

        // 3. 校验学号唯一性
        if (StringUtils.hasText(volunteerProfile.getStudentId())) {
            LambdaQueryWrapper<VolunteerProfile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VolunteerProfile::getStudentId, volunteerProfile.getStudentId());
            // 排除当前用户的情况 (如果是更新操作，且学号就是自己的，则不冲突)
            queryWrapper.ne(VolunteerProfile::getUserId, userId);
            
            if (this.count(queryWrapper) > 0) {
                throw new ServiceException("该学号已被使用");
            }
        } else {
            throw new ServiceException("学号不能为空");
        }

        // 4. 保存或更新到数据库
        // 由于 VolunteerProfile 的主键是 userId (INPUT 类型)，我们需要检查是否存在
        // 实际上 MyBatis-Plus saveOrUpdate 会根据 @TableId 注解判断，
        // 这里 ID 是 userId，如果 userId 对应的记录存在则更新，否则插入
        boolean result = this.saveOrUpdate(volunteerProfile);
        
        if (!result) {
            throw new ServiceException("更新资料失败");
        }

        // 5. 删除 Redis 缓存 (保证数据一致性)
        String redisKey = USER_PROFILE_KEY_PREFIX + userId;
        redisTemplate.delete(redisKey);
    }

    @Autowired
    private com.volunteer.mapper.UserMapper userMapper;

    @Override
    public VolunteerProfile getProfile(Integer userId) {
        // 1. 先查 profile 表 (基础信息，姓名、学号等)
        VolunteerProfile profile = this.getById(userId);
        if (profile == null) {
            // 如果只有账号没填资料，可能返回空，需要初始化一个空对象
            profile = new VolunteerProfile();
            profile.setUserId(userId);
        }
        
        // 2. 查 user 表 (积分、信用)
        com.volunteer.entity.User user = userMapper.selectById(userId);
        if (user != null) {
            // 将 Users 表中的数据填充到 Profile DTO 中返回
            profile.setPoints(user.getPoints());
            profile.setCreditScore(user.getCreditScore());
        }
        
        // 暂停 Redis 缓存逻辑，因为涉及多表组装，且分数变动频繁
        return profile;
    }
}
