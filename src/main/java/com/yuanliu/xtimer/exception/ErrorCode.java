package com.yuanliu.xtimer.exception;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.exception
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 15:17
 * @Version 1.0
 */
public enum ErrorCode {

    SUCCESS(0,"ok"),
    UNKNOWN_ERROR(90001,"未知异常"),
    SYSTEM_ERROR(90002,"系统内部异常"),
    PARAMS_ERROR(90003,"请求参数异常"),
    ;


    private final int code;
    private final String message;

    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }
}
