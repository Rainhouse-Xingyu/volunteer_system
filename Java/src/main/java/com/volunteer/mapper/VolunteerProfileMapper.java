package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.VolunteerProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 志愿者资料 Mapper 接口
 */
@Mapper
public interface VolunteerProfileMapper extends BaseMapper<VolunteerProfile> {
}
