package com.yuanliu.xtimer.exception;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.exception
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 15:20
 * @Version 1.0
 */
public class BusinessException extends RuntimeException{

    private final int code;

    public BusinessException(int code, String message){
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode  errorCode){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode,String message){
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode(){
        return code;
    }
}
