package com.example.siteback.Config;

import lombok.Data;

@Data
public class BaResponse<T> {
    private int code;         // 状态码，例如 200
    private String message;   // 消息，例如 "success"
    private T data;           // 具体数据，泛型支持对象、数组、分页结构等

    public BaResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> BaResponse<T> success(T data) {
        return new BaResponse<>(200, "success", data);
    }

    public static <T> BaResponse<T> error(String message) {
        return new BaResponse<>(500, message, null);
    }

}
