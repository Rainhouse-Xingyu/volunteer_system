package com.volunteer.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义业务异常类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String message;
    
    public ServiceException() {
        super();
    }
    
    public ServiceException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }
    
    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = 500;
    }
    
    public ServiceException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
