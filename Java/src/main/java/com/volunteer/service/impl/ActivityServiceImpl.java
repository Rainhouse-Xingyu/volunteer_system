package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Activity;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.service.ActivityService;
import com.volunteer.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 活动服务实现类
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private com.volunteer.mapper.UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    private static final String ACTIVITY_QUOTA_PREFIX = "activity:quota:";

    private static final String ACTIVITY_DETAIL_PREFIX = "activity:detail:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createActivity(Activity activity, Integer organizerId) {
        // 1. 基础校验
        if (activity.getStartTime() == null || activity.getEndTime() == null) {
            throw new ServiceException("活动时间不能为空");
        }

        if (activity.getEndTime().isBefore(activity.getStartTime())) {
            throw new ServiceException("活动结束时间不能早于开始时间");
        }

        if (activity.getQuota() == null || activity.getQuota() <= 0) {
            throw new ServiceException("招募人数必须大于0");
        }
        
        // 2. 填充默认值和关联信息
        activity.setOrganizerId(organizerId);
        activity.setStatus(0); // 0待审核
        activity.setCreatedAt(LocalDateTime.now());
        
        // 3. 保存到数据库
        boolean result = this.save(activity);
        if (!result) {
            throw new ServiceException("活动发布失败");
        }
        
        // 4. (已移除) 活动发布时不再初始化 Redis 名额，需等待管理员审核通过后初始化
    }

    @Override
    // @Cacheable(value = "activities", key = "'page:' + #current", condition = "#current == 1")
    public IPage<Activity> getPublishedActivities(int current, int size) {
        Page<Activity> page = new Page<>(current, size);
        // 使用 Mapper 自定义 SQL 查询，确保严格过滤 status = 1
        return baseMapper.selectPublishedActivities(page);
    }

    @Override
    public IPage<Activity> searchActivities(int current, int size, String keyword, Integer status) {
        Page<Activity> page = new Page<>(current, size);
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        
        // 动态条件
        if (status != null) {
            queryWrapper.eq(Activity::getStatus, status);
        } else {
            // 默认只查招募中(1)或进行中(2)或已结束(3) -> 对用户可见的状态
            // 但如果 status 传了 null，通常是搜索页，我们默认只展示发布的
            queryWrapper.in(Activity::getStatus, 1, 2, 3); 
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> 
                wrapper.like(Activity::getTitle, keyword)
                       .or()
                       .like(Activity::getContent, keyword)
                       .or()
                       .like(Activity::getLocation, keyword)
            );
        }
        
        // 按时间倒序
        queryWrapper.orderByDesc(Activity::getCreatedAt);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public Activity getActivityDetail(Integer activityId) {
        String key = ACTIVITY_DETAIL_PREFIX + activityId;

        // 1. 先查 Redis
        Object activityObj = redisTemplate.opsForValue().get(key);
        if (activityObj != null) {
            Activity a = (Activity) activityObj;
            // 补充组织者信息（若缓存中没有或缓存失效）
            if (a.getOrganizerName() == null && a.getOrganizerId() != null) {
                com.volunteer.entity.User u = userMapper.selectById(a.getOrganizerId());
                if (u != null) {
                    a.setOrganizerName(u.getNickname() != null ? u.getNickname() : u.getUsername());
                    a.setOrganizerAvatar(u.getAvatarUrl());
                    // 更新缓存
                    redisTemplate.opsForValue().set(key, a, 1, TimeUnit.HOURS);
                }
            }
            return a;
        }

        // 2. Redis 没有，查数据库
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        
        // 补充组织者信息
        if (activity.getOrganizerId() != null) {
            com.volunteer.entity.User u = userMapper.selectById(activity.getOrganizerId());
            if (u != null) {
                activity.setOrganizerName(u.getNickname() != null ? u.getNickname() : u.getUsername());
                activity.setOrganizerAvatar(u.getAvatarUrl());
            }
        }

        // 3. 查到后写入 Redis (设置过期时间 1 小时，防止长期不用的数据占用大量内存)
        try {
            redisTemplate.opsForValue().set(key, activity, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            // 缓存写入失败不应该影响主业务
            e.printStackTrace(); 
        }

        return activity;
    }

    @Override
    public String generateSignToken(Integer activityId, Integer organizerId) {
        // 1. 校验活动是否存在及权限
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        if (!activity.getOrganizerId().equals(organizerId)) {
            throw new ServiceException("您无权操作该活动");
        }

        // 2. 生成随机 Token
        String signToken = java.util.UUID.randomUUID().toString().replace("-", "");

        // 保存到数据库 (持久化记录最后一次生成的Token)
        activity.setQrCodeToken(signToken);
        this.updateById(activity);

        // 3. 存入 Redis (60秒过期) -> 修改为120秒以允许时间窗口重叠
        // 需求 Key: activity:sign_token:{activityId} -> signToken (供前端轮询或查看当前Token)
        String organizerKey = "activity:sign_token:" + activityId;
        stringRedisTemplate.opsForValue().set(organizerKey, signToken, 120, TimeUnit.SECONDS);

        // 核心 Key: activity:sign:{token} -> activityId (供 volunteer 扫码签到反查)
        // 为了兼容 RegistrationServiceImpl 中的 checkIn 逻辑 (使用 redisTemplate<String, Object>),
        // 这里使用 redisTemplate 存储 Integer 类型的 activityId
        String checkInKey = "activity:sign:" + signToken;
        redisTemplate.opsForValue().set(checkInKey, activityId, 120, TimeUnit.SECONDS);

        return signToken;
    }

    @Override
    public IPage<Activity> getMyCreatedActivities(int current, int size, Integer organizerId, String status) {
        Page<Activity> page = new Page<>(current, size);
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Activity::getOrganizerId, organizerId);
        
        // 动态过滤状态
        if (status != null && !status.isEmpty()) {
            java.util.List<Integer> statusList = java.util.Arrays.stream(status.split(","))
                                        .map(Integer::parseInt)
                                        .collect(java.util.stream.Collectors.toList());
            queryWrapper.in(Activity::getStatus, statusList);
        }
        
        queryWrapper.orderByDesc(Activity::getCreatedAt);
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditActivity(Integer activityId, Integer status) {
        // 1. 获取活动
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }

        // 2. 安全性校验：如果活动已经是 已发布(1) 状态，禁止重复审核通过，防止名额重置
        if (Integer.valueOf(1).equals(activity.getStatus()) && Integer.valueOf(1).equals(status)) {
             throw new ServiceException("活动已发布，请勿重复审核，防止名额重置");
        }

        // 3. 更新数据库状态
        activity.setStatus(status);
        boolean updateResult = this.updateById(activity);
        if (!updateResult) {
            throw new ServiceException("审核状态更新失败");
        }

        // 发送审核结果通知
        if (Integer.valueOf(1).equals(status) || Integer.valueOf(4).equals(status)) {
            String noticeTitle = Integer.valueOf(1).equals(status) ? "活动审核通过" : "活动审核驳回";
            String noticeContent = Integer.valueOf(1).equals(status) 
                    ? "恭喜！您发布的活动【" + activity.getTitle() + "】已通过审核并发布。"
                    : "很遗憾，您发布的活动【" + activity.getTitle() + "】未通过审核。";
            
            notificationService.sendNotice(activity.getOrganizerId(), noticeTitle, noticeContent, "system_msg");
        }

        // 4. 如果审核通过 (status=1)，初始化 Redis 库存
        if (Integer.valueOf(1).equals(status)) {
            String quotaKey = ACTIVITY_QUOTA_PREFIX + activity.getActivityId();
            try {
                // 使用 StringRedisTemplate 写入，确保 decrement 操作兼容性
                stringRedisTemplate.opsForValue().set(quotaKey, String.valueOf(activity.getQuota()));
            } catch (Exception e) {
                throw new ServiceException("系统异常：名额初始化失败");
            }
        }
    }
}
