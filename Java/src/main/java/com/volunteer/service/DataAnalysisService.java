package com.volunteer.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.volunteer.vo.ChartVO;

public interface DataAnalysisService {
    /**
     * 导出活动报名名单
     */
    void exportRegistrations(HttpServletResponse response, Integer activityId, Integer organizerId) throws IOException;

    /**
     * 获取活动类型分布 (饼图数据)
     */
    List<ChartVO> getActivityStatusDistribution();
    
    /**
     * 获取近期每日新增用户 (折线图数据)
     */
    Map<String, Object> getUserGrowthTrend();
}
