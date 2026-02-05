package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.dto.RegistrationDTO;
import com.volunteer.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 报名记录 Mapper 接口
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    @Select("SELECT r.*, a.title as activityTitle, a.location, a.start_time, a.end_time " +
            "FROM registrations r " +
            "LEFT JOIN activities a ON r.activity_id = a.activity_id " +
            "WHERE r.volunteer_id = #{userId} " +
            "ORDER BY r.check_in_time DESC")
    IPage<RegistrationDTO> selectMyRegistrations(IPage<RegistrationDTO> page, @Param("userId") Integer userId);
}
