package com.volunteer.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.volunteer.entity.Activity;
import com.volunteer.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 活动状态定时任务
 */
@Slf4j
@Component
public class ActivityStatusTask {

    @Autowired
    private ActivityService activityService;

    /**
     * 每分钟检查一次活动是否已结束
     * 如果当前时间超过结束时间，且状态不是已结束(3)，则更新为已结束
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkActivityStatus() {
        log.info("开始检查活动状态...");
        LambdaUpdateWrapper<Activity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Activity::getStatus, 3) // 设置为已结束
                .lt(Activity::getEndTime, LocalDateTime.now()) // 结束时间小于当前时间
                .ne(Activity::getStatus, 3); // 状态不为已结束

        activityService.update(updateWrapper);
        log.info("活动状态检查完成");
    }
}
