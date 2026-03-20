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
        // 先查询活动信息
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        // 校验活动是否结束
        if (activity.getStatus() == 3 || (activity.getEndTime() != null && activity.getEndTime().isBefore(java.time.LocalDateTime.now()))) {
             throw new ServiceException("活动已结束，无法报名");
        }

        String quotaKey = ACTIVITY_QUOTA_PREFIX + activityId;

        // 检查 Redis 中是否存在 Key，如果不存在，则从数据库同步
        if (Boolean.FALSE.equals(redisTemplate.hasKey(quotaKey))) {
            int remaining = activity.getQuota() - activity.getCurrentParticipants();
            // 使用 setIfAbsent 避免并发覆盖，且只有当名额 > 0 时才有意义（但为了防止缓存穿透，<=0 也设为0）
            redisTemplate.opsForValue().setIfAbsent(quotaKey, Math.max(remaining, 0));
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
            
            // 清除活动详情缓存
            redisTemplate.delete("activity:detail:" + activityId);

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

        // 清除活动详情缓存
        redisTemplate.delete("activity:detail:" + activityId);

        // 4. 从 Redis 报名 Set 中移除用户
        redisTemplate.opsForSet().remove(usersKey, userId);
    }

    @Override
    public IPage<RegistrationDTO> getMyRegistrations(int current, int size, Integer userId, Integer status) {
        // 使用 Mapper 自定义关联查询，一步到位获取 Activity 信息
        Page<RegistrationDTO> page = new Page<>(current, size);
        return baseMapper.selectMyRegistrations(page, userId, status);
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

        // 6. 增加信用分 (1分) (积分改为评估后发放)
        UpdateWrapper<com.volunteer.entity.VolunteerProfile> profileUpdate = new UpdateWrapper<>();
        profileUpdate.setSql("credit_score = ifnull(credit_score, 100) + 1")
                     .eq("user_id", userId);
        volunteerProfileMapper.update(null, profileUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Integer activityId, String signToken, Integer userId) {
        // 1. 校验 Token 时效性 (使用 activity:sign:{token} 以支持多Token共存时间窗口)
        String checkInKey = "activity:sign:" + signToken;
        Object storedActivityId = redisTemplate.opsForValue().get(checkInKey);
        
        if (storedActivityId == null) {
             throw new ServiceException("签到码已过期或无效");
        }

        // 校验活动ID是否匹配
        if (!String.valueOf(storedActivityId).equals(String.valueOf(activityId))) {
             throw new ServiceException("该签到码不属于当前活动");
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

        // 6. 增加信用分 (1分) (积分改为评估后发放)
        UpdateWrapper<com.volunteer.entity.VolunteerProfile> profileUpdate = new UpdateWrapper<>();
        profileUpdate.setSql("credit_score = ifnull(credit_score, 100) + 1")
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
        queryWrapper.eq(Registration::getActivityId, activityId)
                   .orderByDesc(Registration::getCreateTime);
                   
        List<Registration> list = registrationMapper.selectList(queryWrapper);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 补全信息 (与分页查询逻辑一致)
        return list.stream().map(reg -> {
            RegistrationDTO dto = new RegistrationDTO();
            BeanUtils.copyProperties(reg, dto);
            
            // 签到状态
            dto.setCheckinStatus(reg.getCheckinStatus() != null ? reg.getCheckinStatus() : 0);

            // 查询志愿者 Profile
            com.volunteer.entity.VolunteerProfile profile = volunteerProfileMapper.selectById(reg.getVolunteerId());
            if (profile != null) {
                dto.setVolunteerName(profile.getRealName());
                dto.setPhone(profile.getPhone());
                dto.setStudentId(profile.getStudentId());
            } else {
                com.volunteer.entity.User user = userMapper.selectById(reg.getVolunteerId());
                if (user != null) {
                    dto.setVolunteerName(user.getUsername());
                }
            }
            
            // 补全活动标题
            Activity act = activityMapper.selectById(reg.getActivityId());
            if(act != null) {
                dto.setActivityTitle(act.getTitle());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<RegistrationDTO> listByActivity(Integer activityId, int current, int size) {
        // 1. 分页查询该活动的所有报名记录
        Page<Registration> page = new Page<>(current, size);
        LambdaQueryWrapper<Registration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Registration::getActivityId, activityId)
                   .orderByDesc(Registration::getCreateTime);
                   
        IPage<Registration> resultPage = registrationMapper.selectPage(page, queryWrapper);
        List<Registration> list = resultPage.getRecords();
        
        if (list.isEmpty()) {
            return new Page<RegistrationDTO>(current, size);
        }
        
        // 2. 补全信息
        List<RegistrationDTO> dtoList = list.stream().map(reg -> {
            RegistrationDTO dto = new RegistrationDTO();
            BeanUtils.copyProperties(reg, dto);
            // 确保 dto.createTime 存在
            // dto.setCreateTime(reg.getCreateTime()); // RegistrationDTO 可能由 BeanUtils 自动填充

            // 签到状态 (Registration 表自带 checkinStatus)
            dto.setCheckinStatus(reg.getCheckinStatus() != null ? reg.getCheckinStatus() : 0);

            // 查询志愿者 Profile
            com.volunteer.entity.VolunteerProfile profile = volunteerProfileMapper.selectById(reg.getVolunteerId());
            com.volunteer.entity.User user = userMapper.selectById(reg.getVolunteerId());
            
            // 优先设置用户信息
            if (user != null) {
                dto.setAvatarUrl(user.getAvatarUrl());
                // 默认用昵称或用户名
                String name = user.getNickname() != null ? user.getNickname() : user.getUsername();
                dto.setVolunteerName(name);
            }
            
            // 如果有实名信息，覆盖显示
            if (profile != null) {
                if (profile.getRealName() != null && !profile.getRealName().isEmpty()) {
                    dto.setVolunteerName(profile.getRealName());
                }
                dto.setPhone(profile.getPhone());
                dto.setStudentId(profile.getStudentId());
            }
            
            // 补全活动标题 (可选，如果是同一个活动列表其实已知)
            Activity act = activityMapper.selectById(reg.getActivityId());
            if(act != null) {
                dto.setActivityTitle(act.getTitle());
            }

            return dto;
        }).collect(Collectors.toList());

        Page<RegistrationDTO> dtoPage = new Page<>(current, size);
        dtoPage.setRecords(dtoList);
        dtoPage.setTotal(resultPage.getTotal());
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRegistration(Integer registrationId, Integer targetStatus, Integer organizerId) {
        // 1. 查询报名记录
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new ServiceException("报名记录不存在");
        }

        // 2. 权限校验
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            throw new ServiceException("关联活动不存在");
        }
        if (!activity.getOrganizerId().equals(organizerId)) {
            // 如果 organizerId 为空或者不匹配，则无权操作
            throw new ServiceException("您无权审核此活动的报名");
        }

        Integer oldStatus = registration.getRegStatus();
        if (oldStatus.equals(targetStatus)) {
            return; // 状态未变更
        }

        // 3. 更新状态
        registration.setRegStatus(targetStatus);
        registrationMapper.updateById(registration);

        // 4. 库存处理
        // 场景A: 拒绝操作 (status 1 -> 2) => 释放名额
        // 注意：只有原先是占用名额的状态（0待审核 或 1已录用）转为 2拒绝 才释放。
        // 但通常 logic 是：0 => 2 (release), 1 => 2 (release), 2 => 1 (occupy).
        // 且 register() 方法里默认是预占名额 (check logic if needed).
        // 假设 register() 时 quota-1, status=0. 
        // useful if organizer rejects pending application ( 0 -> 2 ) -> release
        // useful if organizer kicks accepted user ( 1 -> 2 ) -> release
        if (targetStatus == 2 && (oldStatus == 1 || oldStatus == 0)) {
             increaseQuota(registration.getActivityId());
        }
        // 场景B: 从已拒绝恢复为录用 (2 -> 1) => 占用名额
        if (targetStatus == 1 && oldStatus == 2) {
             decreaseQuota(registration.getActivityId());
        }
        // 注意：0 -> 1 不需要操作库存，因为注册时已经在 Redis 中扣减了
        
        // 5. 发送通知
        String statusText = (targetStatus == 1) ? "已通过录用" : "未能通过审核";
        String content = String.format("您报名的活动【%s】审核结果：%s。如有疑问请联系组织者。", activity.getTitle(), statusText);
        notificationService.sendNotice(registration.getVolunteerId(), "报名审核通知", content, "system_msg");
    }

    @Override
    public String generateCheckInCode(Integer activityId, Integer organizerId) {
        // 1. 校验权限
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        if (!activity.getOrganizerId().equals(organizerId)) {
             throw new ServiceException("无权操作");
        }
        
        // 2. 生成随机 Token (UUID)
        String token = java.util.UUID.randomUUID().toString().replace("-", "");

        // 保存到数据库
        Activity activity1 = activityMapper.selectById(activityId); // Ensure we have the latest object if needed, or reuse if passed in (not passed in)
        // Check activity not null (checked in step 1)
        activity1.setQrCodeToken(token);
        activityMapper.updateById(activity1);
        
        // 3. 存入Redis，设置有效期 60 秒
        String redisKey = "activity:sign_token:" + activityId;
        stringRedisTemplate.opsForValue().set(redisKey, token, 60, java.util.concurrent.TimeUnit.SECONDS);

        // 兼容旧版扫码 (Token -> ActivityId)
        String checkInKey = "activity:sign:" + token;
        redisTemplate.opsForValue().set(checkInKey, activityId, 60, java.util.concurrent.TimeUnit.SECONDS);
        
        return token;
    }
    
    /**
     * 辅助: 增加库存 (释放名额)
     */
    private void increaseQuota(Integer activityId) {
        // Redis 库存 +1
        String quotaKey = ACTIVITY_QUOTA_PREFIX + activityId;
        redisTemplate.opsForValue().increment(quotaKey);
        
        // 数据库 current_participants -1
        UpdateWrapper<Activity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("current_participants = current_participants - 1")
                     .eq("activity_id", activityId)
                     .gt("current_participants", 0);
        activityMapper.update(null, updateWrapper);
        
        // 释放名额后清除Detail缓存
        redisTemplate.delete("activity:detail:" + activityId);
    }

    /**
     * 辅助: 减少库存 (占用名额)
     */
    private void decreaseQuota(Integer activityId) {
         // Redis 库存 -1
         String quotaKey = ACTIVITY_QUOTA_PREFIX + activityId;
         Long newQuota = redisTemplate.opsForValue().decrement(quotaKey);
         if (newQuota != null && newQuota < 0) {
             // 回滚
             redisTemplate.opsForValue().increment(quotaKey);
             throw new ServiceException("活动名额已满，无法重新录用");
         }
         
         // 数据库 current_participants +1
         UpdateWrapper<Activity> updateWrapper = new UpdateWrapper<>();
         updateWrapper.setSql("current_participants = current_participants + 1")
                      .eq("activity_id", activityId);
         activityMapper.update(null, updateWrapper);
         
         // 增加后清除Detail缓存
         redisTemplate.delete("activity:detail:" + activityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assessVolunteer(Integer registrationId, String assessment, Integer organizerId) {
        // 1. 查询报名记录
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw new ServiceException("报名记录不存在");
        }

        // 2. 校验权限
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        if (!activity.getOrganizerId().equals(organizerId)) {
            throw new ServiceException("您无权对该活动志愿者进行评价");
        }
        
        // 3. 校验状态：必须是已录取且已签到
        if (registration.getRegStatus() != 1) {
            throw new ServiceException("该志愿者未被录用，无法评价");
        }
        if (registration.getCheckinStatus() == null || registration.getCheckinStatus() != 1) {
            throw new ServiceException("该志愿者未签到，无法评价");
        }
        
        // 4. 防止重复评价
        if (registration.getAssessment() != null && !registration.getAssessment().isEmpty()) {
            throw new ServiceException("该志愿者已完成评价，不可重复操作");
        }

        // 5. 更新评价
        registration.setAssessment(assessment);
        registrationMapper.updateById(registration);
        
        // 6. 积分处理
        boolean isFail = "Fail".equalsIgnoreCase(assessment) || "不合格".equals(assessment);
        
        if (!isFail) {
            int points = (activity.getRewardPoints() != null) ? activity.getRewardPoints() : 10;
            UpdateWrapper<com.volunteer.entity.VolunteerProfile> profileUpdate = new UpdateWrapper<>();
            profileUpdate.setSql("points = ifnull(points, 0) + " + points)
                         .eq("user_id", registration.getVolunteerId());
            volunteerProfileMapper.update(null, profileUpdate);
            
            // 发送通知
            String content = String.format("您参加的活动【%s】已被评价为【%s】，获得 %d 积分。", activity.getTitle(), assessment, points);
            notificationService.sendNotice(registration.getVolunteerId(), "活动评价通知", content, "system_msg");
        } else {
            // 不合格，发送通知
            String content = String.format("您参加的活动【%s】已被评价为【%s】，本次不获得积分。", activity.getTitle(), assessment);
            notificationService.sendNotice(registration.getVolunteerId(), "活动评价通知", content, "system_msg");
        }
    }

    @Override
    public long countPendingForOrganizer(Integer organizerId) {
        // Find all activities by this organizer
        List<Activity> activities = activityMapper.selectList(
            new LambdaQueryWrapper<Activity>().eq(Activity::getOrganizerId, organizerId)
        );
        
        if (activities.isEmpty()) {
            return 0;
        }
        
        List<Integer> activityIds = activities.stream().map(Activity::getActivityId).collect(Collectors.toList());
        
        if (activityIds.isEmpty()) {
            return 0;
        }

        return this.count(new LambdaQueryWrapper<Registration>()
            .in(Registration::getActivityId, activityIds)
            .eq(Registration::getRegStatus, 0)
        );
    }
}
