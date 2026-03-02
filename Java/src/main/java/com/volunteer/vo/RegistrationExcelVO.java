package com.volunteer.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class RegistrationExcelVO {
    @ExcelProperty("报名ID")
    private Integer regId;

    @ExcelProperty("活动标题")
    private String activityTitle;

    @ExcelProperty("志愿者姓名")
    private String volunteerName;

    @ExcelProperty("学号")
    private String studentId;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("状态")
    private String statusDesc; // 已录取/待审核等

    @ExcelProperty("是否签到")
    private String checkinDesc;
}
