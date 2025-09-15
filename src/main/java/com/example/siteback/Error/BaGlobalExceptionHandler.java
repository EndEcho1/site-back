package com.example.siteback.Error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class BaGlobalExceptionHandler {
    // 处理 BaErrorException 异常
    @ExceptionHandler(BaErrorException.class)
    public ResponseEntity<BaErrorResponse> handleCustomException(BaErrorException ex, WebRequest request) {

        // 创建一个 ErrorResponse 对象来封装错误信息
        BaErrorResponse errorResponse = new BaErrorResponse(
                ex.getErrorMessage(),  // 异常消息
                ex.getErrorColor(),    // 错误的颜色信息
                ex.getErrorCode(),     // 错误的代码
                ex.getErrorClass()     // 错误来源的类
        );

        // 根据错误代码设置不同的 HTTP 状态码
        HttpStatus status = HttpStatus.BAD_REQUEST; // 默认 400
        if ("404".equals(ex.getErrorCode())) {
            status = HttpStatus.NOT_FOUND;  // 如果是 404 错误，返回 404 状态码
        } else if ("500".equals(ex.getErrorCode())) {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 如果是 500 错误，返回 500 状态码
        }

        return new ResponseEntity<>(errorResponse, status);
    }


    // 可以添加其他异常处理方法

}
