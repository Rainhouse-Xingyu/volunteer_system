package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.User;
import com.volunteer.entity.VolunteerProfile;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.VolunteerProfileMapper;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import com.volunteer.entity.Activity;
import com.volunteer.entity.Registration;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.RegistrationMapper;
import java.time.LocalDateTime;

/**
 * 志愿者服务实现类
 */
@Service
public class VolunteerServiceImpl extends ServiceImpl<VolunteerProfileMapper, VolunteerProfile> implements VolunteerService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;

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
        volunteerProfile.setVolunteerPoints(null);
        volunteerProfile.setCreditScore(null);
        
        // 更新 User 表中的基本信息 (昵称、头像)
        if (StringUtils.hasText(volunteerProfile.getNickname()) || StringUtils.hasText(volunteerProfile.getAvatarUrl())) {
            User user = new User();
            user.setUserId(userId);
            if (StringUtils.hasText(volunteerProfile.getNickname())) {
                user.setNickname(volunteerProfile.getNickname());
            }
            if (StringUtils.hasText(volunteerProfile.getAvatarUrl())) {
                user.setAvatarUrl(volunteerProfile.getAvatarUrl());
            }
            userMapper.updateById(user);
        }

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
            profile.setVolunteerPoints(user.getPoints());
            profile.setCreditScore(user.getCreditScore());
            profile.setNickname(user.getNickname());
            profile.setAvatarUrl(user.getAvatarUrl());
        }
        
        // 暂停 Redis 缓存逻辑，因为涉及多表组装，且分数变动频繁
        return profile;
    }

    @Override
    public Map<String, Object> getLeaderboard(Integer userId) {
        // 1. Get Top 10
        Page<User> page = new Page<>(1, 10);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, "volunteer")
                    .orderByDesc(User::getPoints);
        
        // 优化：只查需要的某些字段
        queryWrapper.select(User::getUserId, User::getUsername, User::getPoints, User::getCreditScore);
        
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        List<User> topList = userPage.getRecords();
        
        // 2. Get My Rank
        User me = userMapper.selectById(userId);
        Integer myRank = 0;
        Integer myPoints = 0;
        
        if (me != null) {
            myPoints = (me.getPoints() == null) ? 0 : me.getPoints();
            // Count users with more points
            // userMapper must have countRank method
            myRank = userMapper.countRank(myPoints);
        }
        
        // 3. Assemble Result
        Map<String, Object> result = new HashMap<>();
        result.put("leaderboard", topList);
        result.put("myRank", myRank);
        result.put("myPoints", myPoints);
        
        return result;
    }

    @Override
    public Activity getRecommendedActivity(Integer userId) {
        // 1. 获取用户已报名的活动（包括待审核和已录取，不包括已拒绝）
        // 获取活动时间段，以便排除冲突
        List<Registration> myRegs = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getVolunteerId, userId)
                .in(Registration::getRegStatus, 0, 1)); // 0:待审, 1:录用 (拒绝的不算冲突)
        
        // 收集已报名的活动ID，用于直接排除
        Set<Integer> registeredActivityIds = new HashSet<>();
        // 收集已报名的时间段
        List<Activity> myActivities = new ArrayList<>();
        
        for (Registration reg : myRegs) {
            registeredActivityIds.add(reg.getActivityId());
            Activity act = activityMapper.selectById(reg.getActivityId());
            if (act != null) {
                myActivities.add(act);
            }
        }
        
        // 2. 查询所有候选活动
        // 条件：
        // - 状态为1 (招募中)
        // - 名额未满 (currentParticipants < quota)
        // - 开始时间在未来
        // - 不在已报名列表中
        List<Activity> candidates = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, 1) // 招募中
                .gt(Activity::getStartTime, LocalDateTime.now()) // 未开始
                .apply("current_participants < quota") // 名额未满
                .notIn(!registeredActivityIds.isEmpty(), Activity::getActivityId, registeredActivityIds)
                .orderByAsc(Activity::getStartTime) // 优先推荐快开始的
                .last("LIMIT 20")); // 取前20个进行过滤
        
        // 3. 过滤时间冲突
        for (Activity candidate : candidates) {
            boolean conflict = false;
            for (Activity myAct : myActivities) {
                // 检查时间是否有重叠
                // (StartA < EndB) and (EndA > StartB)
                if (candidate.getStartTime().isBefore(myAct.getEndTime()) && 
                    candidate.getEndTime().isAfter(myAct.getStartTime())) {
                    conflict = true;
                    break;
                }
            }
            
            if (!conflict) {
                // 找到第一个无冲突的活动，直接返回
                // 这里可以扩展为评分排序机制，但目前简单返回最早的一个
                return candidate;
            }
        }
        
        return null; // 没有合适的推荐
    }
}
