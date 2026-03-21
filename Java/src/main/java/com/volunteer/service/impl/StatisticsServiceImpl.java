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

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

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
        // 宽松统计：已完成(isCompleted=1) 或 已签到(checkinStatus=1)
        long totalHours = 0;
        
        List<Registration> validRegs = registrationMapper.selectList(
            new LambdaQueryWrapper<Registration>()
                .eq(Registration::getIsCompleted, 1)
                .or()
                .eq(Registration::getCheckinStatus, 1)
        );

        for (Registration reg : validRegs) {
            Activity act = activityMapper.selectById(reg.getActivityId());
            if (act != null && act.getStartTime() != null && act.getEndTime() != null) {
                Duration duration = Duration.between(act.getStartTime(), act.getEndTime());
                totalHours += duration.toHours();
            }
        }
        data.put("totalServiceHours", totalHours);

        // 4. 进行中的活动 (包括 1招募中 和 2进行中)
        Long activeActivities = activityMapper.selectCount(
            new LambdaQueryWrapper<Activity>().in(Activity::getStatus, 1, 2)
        );
        data.put("activeActivities", activeActivities);

        return data;
    }

    @Override
    public Map<String, Object> getChartsData() {
        Map<String, Object> result = new HashMap<>();

        // 1. 活动参与度 (Top 10)
        List<Activity> topActivities = activityMapper.selectList(
            new LambdaQueryWrapper<Activity>()
                .in(Activity::getStatus, 1, 2, 3) // 仅统计发布后的活动
                .orderByDesc(Activity::getCurrentParticipants)
                .last("LIMIT 10")
        );
        List<Map<String, Object>> activityStats = new ArrayList<>();
        for (Activity act : topActivities) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", act.getTitle());
            map.put("value", act.getCurrentParticipants() != null ? act.getCurrentParticipants() : 0);
            activityStats.add(map);
        }
        result.put("activityParticipation", activityStats);

        // 2. 服务时长 (近6个月)
        List<String> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            months.add(now.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        
        // 简单实现：查询所有已完成记录并在内存中聚合 (数据量大时应改为SQL GroupBy)
        Map<String, Long> hoursMap = new HashMap<>();
        for (String m : months) hoursMap.put(m, 0L);
        
        List<Registration> completedRegs = registrationMapper.selectList(
            new LambdaQueryWrapper<Registration>().eq(Registration::getIsCompleted, 1)
        );
        
        for (Registration reg : completedRegs) {
            Activity act = activityMapper.selectById(reg.getActivityId());
            if (act != null && act.getEndTime() != null) {
                String month = act.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                if (hoursMap.containsKey(month)) {
                    long hours = Duration.between(act.getStartTime(), act.getEndTime()).toHours();
                    hoursMap.put(month, hoursMap.get(month) + hours);
                }
            }
        }
        List<Map<String, Object>> serviceHoursStats = new ArrayList<>();
        for (String m : months) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", m);
            map.put("value", hoursMap.get(m));
            serviceHoursStats.add(map);
        }
        result.put("serviceHours", serviceHoursStats);

        // 3. 积分分布
        List<User> volunteers = userMapper.selectList(
            new LambdaQueryWrapper<User>().eq(User::getRole, "VOLUNTEER")
        );
        // Ranges: 0-50, 51-100, 101-200, 201-500, >500
        int[] counts = new int[5]; 
        String[] ranges = {"0-50", "51-100", "101-200", "201-500", ">500"};
        
        for (User u : volunteers) {
            int p = u.getPoints() != null ? u.getPoints() : 0;
            if (p <= 50) counts[0]++;
            else if (p <= 100) counts[1]++;
            else if (p <= 200) counts[2]++;
            else if (p <= 500) counts[3]++;
            else counts[4]++;
        }
        
        List<Map<String, Object>> pointsStats = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", ranges[i]);
            map.put("value", counts[i]);
            pointsStats.add(map);
        }
        result.put("pointsDistribution", pointsStats);

        return result;
    }
}
