package com.example.SiteBack.Error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.awt.*;


@ToString
@Getter
@AllArgsConstructor
public enum BaErrorMessagesE {

    UNKNOWN_ERROR
            ("An unknown error has occurred",
            Color.red, "X", "UnknownError"),
    INVALID_INPUT
            ("Invalid input",
            Color.blue, "I", "InvalidInput"),
    NOT_FOUND
            ("Not found",
            Color.yellow, "IV", "NotFound"),
    DATABASE_ERROR
            ("Database error",
            Color.orange, "IX", "DatabaseError"),;



    private final String errorMessage;
    private final Color errorColor;
    private final String errorCode;
    private final String errorClass;

    public String getHexColor() {
        return String.format("#%02x%02x%02x",
                errorColor.getRed(), errorColor.getGreen(), errorColor.getBlue());
    }

}
