package com.example.SiteBack.Error;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaErrorResponse {
    private String errorMessage;
    private String errorColor;
    private String errorCode;
    private String errorClass;



}
