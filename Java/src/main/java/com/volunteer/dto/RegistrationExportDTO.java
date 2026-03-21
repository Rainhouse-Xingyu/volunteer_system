package com.volunteer.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 报名签到导出 DTO
 */
@Data
public class RegistrationExportDTO implements Serializable {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("签到状态")
    private String checkInStatus;

    @ExcelProperty("时长(小时)")
    private Double duration;

    @ExcelProperty("积分")
    private Integer points;
}
