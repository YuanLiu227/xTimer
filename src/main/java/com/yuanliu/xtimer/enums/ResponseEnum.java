package com.yuanliu.xtimer.enums;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.common
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 15:00
 * @Version 1.0
 */
public enum ResponseEnum {
    OK(0, "ok"),
    FAIL(1, "fail"),
    ;

    private final int code;
    private final String message;

    ResponseEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code(){
        return code;
    }

    public String message(){
        return message;
    }
}


