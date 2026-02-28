package com.volunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Evaluation;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.EvaluationMapper;
import com.volunteer.service.EvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createEvaluation(Evaluation evaluation, Integer userId) {
        // 设置评价人ID
        evaluation.setFromUserId(userId);
        
        // 简单校验
        if (evaluation.getToUserId() == null) {
            throw new ServiceException("被评价对象不能为空");
        }
        if (evaluation.getActivityId() == null) {
            throw new ServiceException("关联活动不能为空");
        }
        
        // 设置默认时间
        if (evaluation.getCreatedAt() == null) {
            evaluation.setCreatedAt(LocalDateTime.now());
        }

        // 保存评价
        this.save(evaluation);
    }
}
