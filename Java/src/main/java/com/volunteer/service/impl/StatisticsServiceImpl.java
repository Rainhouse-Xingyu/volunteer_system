package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.entity.Activity;
import com.volunteer.entity.Registration;
import com.volunteer.entity.User;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.RegistrationMapper;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> data = new HashMap<>();

        // 1. 总用户数
        Long totalUsers = userMapper.selectCount(null);
        data.put("totalUsers", totalUsers);

        // 2. 总活动数
        Long totalActivities = activityMapper.selectCount(new LambdaQueryWrapper<Activity>());
        data.put("totalActivities", totalActivities);

        // 3. 累计志愿服务时长 (估算：已完成的报名 * (活动结束时间 - 活动开始时间))
        // 注意：这里仅统计已完成的(is_completed=1 或 reg_status=1 且过了时间)
        // 简单起见，我们查询所有已完成的报名，并关联活动时间
        // 在实际生产中应该写 SQL 聚合，这里用 Java 循环实现简单逻辑（数据量少时）
        
        long totalHours = 0;
        
        // 查询所有已完成的报名 (假设 reg_status=1 且 is_completed=1)
        List<Registration> completedRegs = registrationMapper.selectList(
            new LambdaQueryWrapper<Registration>().eq(Registration::getIsCompleted, 1)
        );

        for (Registration reg : completedRegs) {
            Activity act = activityMapper.selectById(reg.getActivityId());
            if (act != null && act.getStartTime() != null && act.getEndTime() != null) {
                Duration duration = Duration.between(act.getStartTime(), act.getEndTime());
                totalHours += duration.toHours();
            }
        }
        data.put("totalServiceHours", totalHours);

        // 4. 进行中的活动
        Long activeActivities = activityMapper.selectCount(
            new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, 2) // 2进行中
        );
        data.put("activeActivities", activeActivities);

        return data;
    }
}
