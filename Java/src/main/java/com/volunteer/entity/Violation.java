package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 违规处理记录表
 */
@Data
@NoArgsConstructor
@TableName("violations")
public class Violation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 违规记录ID
     */
    @TableId(value = "violation_id", type = IdType.AUTO)
    private Integer violationId;

    /**
     * 违规对象类型(user/activity)
     */
    private String targetType;

    /**
     * 对应ID
     */
    private Integer targetId;

    /**
     * 处理原因
     */
    private String reason;

    /**
     * 操作管理员ID
     */
    private Integer handlerId;

    /**
     * 处理时间
     */
    private LocalDateTime processedAt;
}
