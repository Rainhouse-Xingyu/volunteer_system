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

    @Select("<script>" +
            "SELECT r.*, a.title as activityTitle, a.location, a.start_time, a.end_time, " +
            "(SELECT COUNT(1) FROM comments c WHERE c.target_id = r.activity_id AND c.user_id = r.volunteer_id AND c.target_type = 'activity') > 0 AS has_commented " +
            "FROM registrations r " +
            "LEFT JOIN activities a ON r.activity_id = a.activity_id " +
            "WHERE r.volunteer_id = #{userId} " +
            "<if test='status != null'> AND r.reg_status = #{status} </if>" +
            "ORDER BY r.create_time DESC" +
            "</script>")
    IPage<RegistrationDTO> selectMyRegistrations(IPage<RegistrationDTO> page, @Param("userId") Integer userId, @Param("status") Integer status);
}
