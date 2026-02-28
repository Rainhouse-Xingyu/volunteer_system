package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.Violation;

public interface ViolationService extends IService<Violation> {
    
    /**
     * 提交举报
     */
    void reportViolation(Violation violation, Integer userId);

    /**
     * 分页查询违规记录
     */
    IPage<Violation> getViolationList(Page<Violation> page);

    /**
     * 处理违规
     */
    void processViolation(Integer violationId, String reason, Integer handlerId);
}
