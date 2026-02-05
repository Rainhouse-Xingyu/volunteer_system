package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 活动 Mapper 接口
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    
    @Select("SELECT * FROM activities WHERE status = 1 ORDER BY created_at DESC")
    IPage<Activity> selectPublishedActivities(IPage<Activity> page);
}
