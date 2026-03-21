package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户信息修改审核表
 */
@Data
@NoArgsConstructor
@TableName(value = "user_updates", autoResultMap = true)
public class UserUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    /**
     * volunteer_profile / organizer_profile / user_info
     */
    private String type;

    /**
     * 原数据 (JSON)
     */
    @TableField(value = "original_data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> originalData;

    /**
     * 修改后数据 (JSON)
     */
    @TableField(value = "modified_data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> modifiedData;

    /**
     * 0:待审核, 1:已通过, 2:已拒绝
     */
    private Integer status;

    private String auditReason;

    private Integer adminId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
