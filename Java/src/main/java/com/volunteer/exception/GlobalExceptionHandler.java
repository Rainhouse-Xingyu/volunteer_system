package com.volunteer.exception;

import com.volunteer.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public Result<?> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error("请求地址 '{}', 发生业务异常：{}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("请求地址 '{}', 参数校验失败：{}", request.getRequestURI(), errorMsg);
        return Result.error(400, errorMsg);
    }
    
    /**
     * 处理表单绑定异常（@Validated）
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String errorMsg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("请求地址 '{}', 参数绑定失败：{}", request.getRequestURI(), errorMsg);
        return Result.error(400, errorMsg);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("请求地址 '{}', 非法参数：{}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(400, e.getMessage());
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("请求地址 '{}', 发生空指针异常", request.getRequestURI(), e);
        return Result.error("系统内部错误，请联系管理员");
    }
    
    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("请求地址 '{}', 发生未知异常：{}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统异常，请稍后重试");
    }
}
