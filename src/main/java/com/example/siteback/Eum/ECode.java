package com.example.siteback.Eum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ECode {

    E_CODE("E-CODE"),
    E_CODE_MSG("E-CODE-MSG");

    // 新增的字段来表示 HTTP 头名称
    private final String headerE_NAME;

}

