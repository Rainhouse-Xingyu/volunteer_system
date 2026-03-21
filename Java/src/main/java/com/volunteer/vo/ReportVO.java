package com.volunteer.vo;

import lombok.Data;

@Data
public class ReportVO {
    private Integer commentId;
    private String reason;
    private String detail;
    private Integer reporterId; // Optional from client side if inferred from token
}