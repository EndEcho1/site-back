package com.example.siteback.Error;

import lombok.Getter;

@Getter
public class BaErrorException extends RuntimeException{
    private final String errorColor;
    private final String errorCode;
    private final String errorClass;
    private final String errorMessage;

    public BaErrorException(String errorColor,String errorCode,String errorClass,String errorMessage){
        super(errorMessage);// 调用父类 Exception 的构造方法
        this.errorColor = errorColor;  // 初始化
        this.errorCode = errorCode;
        this.errorClass = errorClass;
        this.errorMessage = errorMessage;
    }
    @Override
    public String getMessage() {
        return super.getMessage() + " -Color: " + errorColor + "-Code: " + errorCode ;
    }
}
