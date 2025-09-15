package com.example.siteback.Error;

public class BaErrorResponse {
    private String errorMessage;
    private String errorColor;
    private String errorCode;
    private String errorClass;

    public BaErrorResponse(String errorMessage, String errorColor, String errorCode, String errorClass) {
        this.errorMessage = errorMessage;
        this.errorColor = errorColor;
        this.errorCode = errorCode;
        this.errorClass = errorClass;
    }

    // Getter and Setter methods
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorColor() {
        return errorColor;
    }

    public void setErrorColor(String errorColor) {
        this.errorColor = errorColor;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorClass() {
        return errorClass;
    }

    public void setErrorClass(String errorClass) {
        this.errorClass = errorClass;
    }
}
