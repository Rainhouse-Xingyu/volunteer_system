package com.volunteer.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.entity.Activity;
import com.volunteer.entity.Registration;
import com.volunteer.entity.User;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.RegistrationMapper;
import com.volunteer.mapper.UserMapper;
import com.volunteer.service.DataAnalysisService;
import com.volunteer.service.RegistrationService;
import com.volunteer.vo.ChartVO;
import com.volunteer.vo.RegistrationExcelVO;
import com.volunteer.dto.RegistrationDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataAnalysisServiceImpl implements DataAnalysisService {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public void exportRegistrations(HttpServletResponse response, Integer activityId, Integer organizerId) throws IOException {
        // 1. 权限校验
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        if (!activity.getOrganizerId().equals(organizerId)) {
            throw new ServiceException("无权导出该活动数据");
        }

        // 2. 获取数据
        List<RegistrationDTO> list = registrationService.getActivityRegistrations(activityId);

        // 3. 转换为 Excel VO
        List<RegistrationExcelVO> excelList = list.stream().map(dto -> {
            RegistrationExcelVO vo = new RegistrationExcelVO();
            vo.setRegId(dto.getRegId());
            vo.setActivityTitle(activity.getTitle()); // 所有人同一个活动
            vo.setVolunteerName(dto.getVolunteerName());
            vo.setStudentId(dto.getStudentId());
            vo.setPhone(dto.getPhone());
            
            // 状态描述
            String status = "未知";
            if (dto.getRegStatus() == 0) status = "待审核";
            else if (dto.getRegStatus() == 1) status = "已录取";
            else if (dto.getRegStatus() == 2) status = "已拒绝";
            else if (dto.getRegStatus() == 3) status = "已取消";
            vo.setStatusDesc(status);

            // 签到描述
            vo.setCheckinDesc(dto.getCheckinStatus() != null && dto.getCheckinStatus() == 1 ? "是" : "否");
            return vo;
        }).collect(Collectors.toList());

        // 4. 写出 Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode(activity.getTitle() + "_报名名单", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), RegistrationExcelVO.class)
                 .sheet("名单")
                 .doWrite(excelList);
    }

    @Override
    public List<ChartVO> getActivityStatusDistribution() {
        // 统计不同状态的活动数量
        QueryWrapper<Activity> wrapper = new QueryWrapper<>();
        wrapper.select("status", "count(*) as total").groupBy("status");
        
        List<Map<String, Object>> list = activityMapper.selectMaps(wrapper);
        
        return list.stream().map(map -> {
            ChartVO vo = new ChartVO();
            Integer status = (Integer) map.get("status");
            Long count = (Long) map.get("total");
            
            String name = "未知";
            if (status == 0) name = "待审核";
            else if (status == 1) name = "招募中";
            else if (status == 2) name = "进行中";
            else if (status == 3) name = "已结束";
            
            vo.setName(name);
            vo.setValue(count);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserGrowthTrend() {
        // 模拟近7天数据 (真实场景需写SQL按天Group By)
        // 这里简化为直接返回模拟数据供前端 ECharts 展示
        
        Map<String, Object> result = new HashMap<>();
        // X轴: 日期
        List<String> dates = Arrays.asList("02-22", "02-23", "02-24", "02-25", "02-26", "02-27", "02-28");
        // Y轴: 新增人数
        List<Integer> values = Arrays.asList(5, 8, 12, 7, 15, 20, 18);
        
        result.put("dates", dates);
        result.put("values", values);
        return result;
    }
}
