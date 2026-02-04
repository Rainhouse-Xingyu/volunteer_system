package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Activity;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.service.ActivityService;
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

        // 4. 将活动名额存入 Redis (Key: activity:quota:{activityId})
        String quotaKey = ACTIVITY_QUOTA_PREFIX + activity.getActivityId();
        try {
            redisTemplate.opsForValue().set(quotaKey, activity.getQuota());
        } catch (Exception e) {
            // Redis 操作失败，为了数据一致性，建议抛出异常回滚数据库
            throw new ServiceException("系统异常：活动名额缓存失败");
        }
    }

    @Override
    @Cacheable(value = "activities", key = "'page:' + #current", condition = "#current == 1")
    public IPage<Activity> getPublishedActivities(int current, int size) {
        Page<Activity> page = new Page<>(current, size);
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        // 查询状态为 1 (已发布) 的活动
        queryWrapper.eq(Activity::getStatus, 1)
                    .orderByDesc(Activity::getCreatedAt);
        return this.page(page, queryWrapper);
    }

    @Override
    public Activity getActivityDetail(Integer activityId) {
        String key = ACTIVITY_DETAIL_PREFIX + activityId;

        // 1. 先查 Redis
        Object activityObj = redisTemplate.opsForValue().get(key);
        if (activityObj != null) {
            return (Activity) activityObj;
        }

        // 2. Redis 没有，查数据库
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new ServiceException("活动不存在");
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

        // 3. 存入 Redis (60秒过期)
        // 需求 Key: activity:sign_token:{activityId} -> signToken (供前端轮询或查看当前Token)
        String organizerKey = "activity:sign_token:" + activityId;
        stringRedisTemplate.opsForValue().set(organizerKey, signToken, 60, TimeUnit.SECONDS);

        // 核心 Key: activity:sign:{token} -> activityId (供 volunteer 扫码签到反查)
        // 为了兼容 RegistrationServiceImpl 中的 checkIn 逻辑 (使用 redisTemplate<String, Object>),
        // 这里使用 redisTemplate 存储 Integer 类型的 activityId
        String checkInKey = "activity:sign:" + signToken;
        redisTemplate.opsForValue().set(checkInKey, activityId, 60, TimeUnit.SECONDS);

        return signToken;
    }
}
