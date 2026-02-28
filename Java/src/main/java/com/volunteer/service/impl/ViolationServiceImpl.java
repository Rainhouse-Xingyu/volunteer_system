package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.Violation;
import com.volunteer.exception.ServiceException;
import com.volunteer.mapper.ViolationMapper;
import com.volunteer.service.ViolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ViolationServiceImpl extends ServiceImpl<ViolationMapper, Violation> implements ViolationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportViolation(Violation violation, Integer userId) {
        if (violation.getTargetId() == null || violation.getTargetType() == null) {
            throw new ServiceException("举报对象不能为空");
        }
        // 这里暂时不存举报人ID到violation表，因为表结构里好像没设计reporter_id，只有handler_id
        // 如果需要记录举报人，可以在Violation实体加字段，或者暂时忽略
        
        // 设置默认状态? entity 没 status 字段，看 db.sql 只有 processed_at
        // 我们假设只要 processed_at 为空就是未处理
        
        this.save(violation);
    }

    @Override
    public IPage<Violation> getViolationList(Page<Violation> page) {
        return this.page(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processViolation(Integer violationId, String reason, Integer handlerId) {
        Violation violation = this.getById(violationId);
        if (violation == null) {
            throw new ServiceException("记录不存在");
        }
        
        // 更新处理信息
        violation.setHandlerId(handlerId);
        violation.setReason(reason); // 处理结果/备注
        violation.setProcessedAt(LocalDateTime.now());
        
        this.updateById(violation);
    }
}
