package com.example.SiteBack.Error;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class BaErrorException extends RuntimeException{


    private final String errorColor;
    private final String errorCode;
    private final String errorClass;

    public BaErrorException(BaErrorMessagesE errorEnum){
        super(errorEnum.getErrorMessage());// 调用父类 Exception 的构造方法
        this.errorColor = errorEnum.getHexColor();
        this.errorCode = errorEnum.getErrorCode();
        this.errorClass = errorEnum.getErrorClass();
    }
    @Override
    public String getMessage() {
        return super.getMessage()
                + " -Color: " + errorColor
                + "-Code: " + errorCode ;
    }



    // 可以添加一个方法来获取原始的错误枚举
    // private final BaErrorMessagesE errorType; // 可以添加这个字段
    // public BaErrorException(BaErrorMessagesE errorEnum) {
    //     super(errorEnum.getErrorMessage());
    //     this.errorType = errorEnum;
    //     this.errorColor = errorEnum.getHexColor();
    //     this.errorCode = errorEnum.getErrorCode();
    //     this.errorClass = errorEnum.getErrorClass();
    // }
    // public BaErrorMessagesE getErrorType() {
    //     return errorType;
    // }
}
