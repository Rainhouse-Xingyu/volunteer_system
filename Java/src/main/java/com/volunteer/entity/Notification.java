package com.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资讯与系统通知表
 */
@Data
@NoArgsConstructor
@TableName("notifications")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "notice_id", type = IdType.AUTO)
    private Integer noticeId;

    /**
     * 发送者(0系统)
     */
    private Integer senderId;

    /**
     * 接收者(0全平台)
     */
    private Integer receiverId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 资讯/系统通知/公告
     */
    private String type;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
