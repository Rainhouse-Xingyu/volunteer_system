package com.volunteer.service;

import java.util.Map;

public interface StatisticsService {
    /**
     * 获取系统概览数据
     */
    Map<String, Object> getOverview();
}
