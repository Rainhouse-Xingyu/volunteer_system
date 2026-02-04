package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.Registration;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报名记录 Mapper 接口
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
}
