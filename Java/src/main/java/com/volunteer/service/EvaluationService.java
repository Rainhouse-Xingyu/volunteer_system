package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Evaluation;

public interface EvaluationService extends IService<Evaluation> {
    /**
     * 发表评价
     * @param evaluation 评价信息
     * @param userId 当前操作人ID
     */
    void createEvaluation(Evaluation evaluation, Integer userId);
}
