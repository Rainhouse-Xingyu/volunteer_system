package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.dto.RegistrationDTO;
import com.volunteer.entity.Activity;
import com.volunteer.entity.Registration;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.RegistrationMapper;
import com.volunteer.entity.Notification;
import com.volunteer.service.NotificationService;
import com.volunteer.service.RegistrationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报名服务实现类
 */
@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private com.volunteer.mapper.VolunteerProfileMapper volunteerProfileMapper;
    
    @Autowired
    private com.volunteer.mapper.UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    private static final String ACTIVITY_QUOTA_PREFIX = "activity:quota:";
    private static final String ACTIVITY_USERS_PREFIX = "activity:users:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(Integer activityId, Integer userId) {
        String quotaKey = ACTIVITY_QUOTA_PREFIX + activityId;

        // 检查 Redis 中是否存在 Key，如果不存在，则从数据库同步
        if (Boolean.FALSE.equals(redisTemplate.hasKey(quotaKey))) {
            Activity activity = activityMapper.selectById(activityId);
            if (activity != null) {
                int remaining = activity.getQuota() - activity.getCurrentParticipants();
                // 使用 setIfAbsent 避免并发覆盖，且只有当名额 > 0 时才有意义（但为了防止缓存穿透，<=0 也设为0）
                redisTemplate.opsForValue().setIfAbsent(quotaKey, Math.max(remaining, 0));
            }
        }
        
        // 1. Redis 原子性递减 (预减库存)
        Long stock = redisTemplate.opsForValue().decrement(quotaKey);

        // 防止 Redis Key 不存在导致返回 null (-1 in auto-boxing/unboxing potentially if not handled, but increment returns Long)
        // 其实 decrement 如果 key 不存在会先初始化为0再减，变成 -1
        // 为了稳健，如果 stock 本身极小(比如 < -100)，可能是被恶意刷的，或者 key 刚初始化。
        // 为了配合 initActivityQuota，如果活动发布时已经 set 了，这里直接 decrement 是安全的。
        
        if (stock == null) {
            // 异常情况，恢复并报错
             throw new ServiceException("系统异常：名额信息丢失");
        }

        if (stock < 0) {
            // 名额已满，回滚 Redis (补偿)
            redisTemplate.opsForValue().increment(quotaKey);
            throw new ServiceException("名额已满");
        }

        // 2. 数据库业务处理
        try {
            // 2.1 检查是否重复报名
            LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Registration::getActivityId, activityId)
                        .eq(Registration::getVolunteerId, userId)
                        .ne(Registration::getRegStatus, 3); // 排除已取消
            if (this.count(queryWrapper) > 0) {
                // 重复报名，业务异常，需要回滚 Redis
                 throw new ServiceException("您已报名该活动");
            }

            // 2.2 插入报名记录
            Registration registration = new Registration();
            registration.setActivityId(activityId);
            registration.setVolunteerId(userId);
            registration.setRegStatus(0); // 默认 0-待审核
            this.save(registration);

            // 2.3 乐观锁更新活动表当前人数
            // UPDATE activities SET current_participants = current_participants + 1 
            // WHERE activity_id = ? AND current_participants < quota
            UpdateWrapper<Activity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.setSql("current_participants = current_participants + 1")
                         .eq("activity_id", activityId)
                         .apply("current_participants < quota");
            
            int rows = activityMapper.update(null, updateWrapper);
            if (rows == 0) {
                 throw new ServiceException("报名失败：名额已满或数据冲突");
            }
            
            // 可选：更新 Redis Set 记录用户
            String usersKey = ACTIVITY_USERS_PREFIX + activityId;
            redisTemplate.opsForSet().add(usersKey, userId);

        } catch (Exception e) {
            // 数据库操作失败，回滚 Redis 库存
            redisTemplate.opsForValue().increment(quotaKey);
            // 重新抛出异常以触发事务回滚
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            }
            throw new ServiceException("报名失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRegistration(Integer activityId, Integer userId) {
        String quotaKey = ACTIVITY_QUOTA_PREFIX + activityId;
        String usersKey = ACTIVITY_USERS_PREFIX + activityId;
        
        // 1. 检查是否报名
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getActivityId, activityId)
                   .eq(Registration::getVolunteerId, userId)
                   .ne(Registration::getRegStatus, 3); // 排除已取消的
        
        Registration registration = this.getOne(queryWrapper);
        if (registration == null) {
            throw new ServiceException("未找到有效报名记录");
        }

        // 2. 更新数据库状态为 3 (已取消)
        registration.setRegStatus(3);
        boolean updateResult = this.updateById(registration);
        
        if (!updateResult) {
            throw new ServiceException("取消报名失败");
        }

        // 3. Redis 库存回补 (原子操作 INCR)
        redisTemplate.opsForValue().increment(quotaKey);

        // 4. 从 Redis 报名 Set 中移除用户
        redisTemplate.opsForSet().remove(usersKey, userId);
    }

    @Override
    public IPage<RegistrationDTO> getMyRegistrations(int current, int size, Integer userId) {
        // 使用 Mapper 自定义关联查询，一步到位获取 Activity 信息
        Page<RegistrationDTO> page = new Page<>(current, size);
        return baseMapper.selectMyRegistrations(page, userId);
    }

    /**
     * 辅助方法：初始化活动库存至 Redis
     */
    @Override
    public void initActivityQuota(Integer activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        String quotaKey = ACTIVITY_QUOTA_PREFIX + activityId;
        redisTemplate.opsForValue().setIfAbsent(quotaKey, activity.getQuota());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(String signToken, Integer userId) {
        // 1. 校验 signToken 有效性 (Redis)
        String redisKey = "activity:sign:" + signToken;
        Object activityIdObj = redisTemplate.opsForValue().get(redisKey);
        
        if (activityIdObj == null) {
            throw new ServiceException("签到二维码失效或错误");
        }
        
        Integer activityId = Integer.valueOf(activityIdObj.toString());

        // 2. 检查用户报名状态
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getActivityId, activityId)
                    .eq(Registration::getVolunteerId, userId);
        
        Registration registration = registrationMapper.selectOne(queryWrapper);
        
        if (registration == null) {
            throw new ServiceException("您未报名该活动");
        }
        
        // 3. 校验状态 (必须是已录取 status=1)
        if (registration.getRegStatus() != 1) {
            throw new ServiceException("您未被该活动录用，无法签到");
        }
        
        // 4. 检查是否已签到
        if (registration.getCheckinStatus() != null && registration.getCheckinStatus() == 1) {
            throw new ServiceException("请勿重复签到");
        }
        
        // 5. 执行签到
        registration.setCheckinStatus(1);
        registration.setCheckInTime(java.time.LocalDateTime.now());
        
        registrationMapper.updateById(registration);

        // 6. 增加积分 (10分) 和 信用分 (1分) -> 更新 volunteer_profiles 表
        UpdateWrapper<com.volunteer.entity.VolunteerProfile> profileUpdate = new UpdateWrapper<>();
        profileUpdate.setSql("points = ifnull(points, 0) + 10, credit_score = ifnull(credit_score, 100) + 1")
                     .eq("user_id", userId);
        volunteerProfileMapper.update(null, profileUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Integer activityId, String signToken, Integer userId) {
        // 1. 校验 Token 时效性
        String redisKey = "activity:sign_token:" + activityId;
        String serverToken = stringRedisTemplate.opsForValue().get(redisKey);
        
        if (serverToken == null || !serverToken.equals(signToken)) {
            throw new ServiceException("签到码已过期或无效");
        }
        
        // 2. 检查用户报名状态
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getActivityId, activityId)
                    .eq(Registration::getVolunteerId, userId);
        
        Registration registration = registrationMapper.selectOne(queryWrapper);
        
        if (registration == null) {
            throw new ServiceException("您未报名该活动");
        }
        
        // 3. 校验状态 (必须是已录取 status=1)
        if (registration.getRegStatus() == 0) {
            throw new ServiceException("您的报名尚未经过审核，请联系组织者");
        }
        if (registration.getRegStatus() == 2) {
            throw new ServiceException("您的报名已被拒绝，无法参加本次活动");
        }
        if (registration.getRegStatus() != 1) {
            throw new ServiceException("您未被该活动录用，无法签到");
        }
        
        // 4. 检查是否已签到
        if (registration.getCheckinStatus() != null && registration.getCheckinStatus() == 1) {
            throw new ServiceException("您已经签到过了，请勿重复操作");
        }
        
        // 5. 执行签到
        registration.setCheckinStatus(1);
        registration.setCheckInTime(java.time.LocalDateTime.now());
        
        registrationMapper.updateById(registration);

        // 6. 增加积分 (10分) 和 信用分 (1分) -> 更新 volunteer_profiles 表
        UpdateWrapper<com.volunteer.entity.VolunteerProfile> profileUpdate = new UpdateWrapper<>();
        profileUpdate.setSql("points = ifnull(points, 0) + 10, credit_score = ifnull(credit_score, 100) + 1")
                     .eq("user_id", userId);
        volunteerProfileMapper.update(null, profileUpdate);
    }

    @Override
    public RegistrationDTO getRegistrationStatus(Integer activityId, Integer userId) {
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getActivityId, activityId)
                    .eq(Registration::getVolunteerId, userId);
        
        Registration registration = registrationMapper.selectOne(queryWrapper);
        if (registration == null) {
            return null;
        }

        RegistrationDTO dto = new RegistrationDTO();
        BeanUtils.copyProperties(registration, dto);
        
        // 简单填充活动标题方便前端展示
        Activity activity = activityMapper.selectById(activityId);
        if (activity != null) {
            dto.setActivityTitle(activity.getTitle());
        }
        
        return dto;
    }

    @Override
    public List<RegistrationDTO> getActivityRegistrations(Integer activityId) {
        // 1. 查询该活动的所有报名记录
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getActivityId, activityId);
        List<Registration> list = registrationMapper.selectList(queryWrapper);
        
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 补全志愿者信息
        return list.stream().map(reg -> {
            RegistrationDTO dto = new RegistrationDTO();
            BeanUtils.copyProperties(reg, dto);
            
            // 查询志愿者 Profile
            com.volunteer.entity.VolunteerProfile profile = volunteerProfileMapper.selectById(reg.getVolunteerId());
            if (profile != null) {
                dto.setVolunteerName(profile.getRealName());
                dto.setPhone(profile.getPhone());
                dto.setStudentId(profile.getStudentId());
            } else {
                // 如果没有 Profile，尝试获取 Username
                com.volunteer.entity.User user = userMapper.selectById(reg.getVolunteerId());
                if (user != null) {
                    dto.setVolunteerName(user.getUsername());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRegistration(Integer registrationId, Integer targetStatus, Integer organizerId) {
        // 1. 查询报名记录
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new ServiceException("报名记录不存在");
        }

        // 2. 校验权限 (检查活动是否属于该组织者)
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            throw new ServiceException("关联活动不存在");
        }
        if (!activity.getOrganizerId().equals(organizerId)) {
            throw new ServiceException("您无权操作该报名记录");
        }

        Integer oldStatus = registration.getRegStatus();
        if (oldStatus.equals(targetStatus)) {
            return; // 状态未变更，直接返回
        }


        // 插入通知
        String statusStr = (targetStatus == 1) ? "已通过" : "被拒绝";
        String content = "您报名的活动【" + activity.getTitle() + "】" + statusStr + "，请进入个人中心查看";
        notificationService.sendNotice(registration.getVolunteerId(), "活动报名结果提醒", content, "通知");

        // 3. 更新状态
        registration.setRegStatus(targetStatus);
        registrationMapper.updateById(registration);

        // 4. 拒绝逻辑：回滚库存
        // 只有当变更为“已拒绝 (2)”且原状态为“已录取 (1)”时，才释放名额
        if (targetStatus == 2 && oldStatus == 1) {
             // 4.1 Redis 库存 +1
             String quotaKey = ACTIVITY_QUOTA_PREFIX + activity.getActivityId();
             redisTemplate.opsForValue().increment(quotaKey);
             
             // 4.2 数据库 current_participants -1
             UpdateWrapper<Activity> updateWrapper = new UpdateWrapper<>();
             updateWrapper.setSql("current_participants = current_participants - 1")
                          .eq("activity_id", activity.getActivityId())
                          // 只有大于0才减，兜底防止负数
                          .gt("current_participants", 0);
             activityMapper.update(null, updateWrapper);
        }
        // 如果是从 2(拒绝) 改回 1(录取)? 用户需求没说，暂不处理复杂的反向回滚逻辑，尽量简化。
    }
}
