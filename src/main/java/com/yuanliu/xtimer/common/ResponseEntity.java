package com.yuanliu.xtimer.common;

import com.yuanliu.xtimer.enums.ResponseEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.common
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:56
 * @Version 1.0
 */
public class ResponseEntity<T> implements Serializable {
    private int code;
    private String message;
    private String datetime;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ResponseEntity<T> ok(T data){
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setData(data);
        responseEntity.setCode(ResponseEnum.OK.code());
        responseEntity.setDatetime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return responseEntity;
    }

    public static <T> ResponseEntity<T> ok(){
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setCode(ResponseEnum.OK.code());
        responseEntity.setMessage(ResponseEnum.OK.message());
        return responseEntity;
    }

    public static <T> ResponseEntity<T> fail(){
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setCode(ResponseEnum.FAIL.code());
        responseEntity.setMessage(ResponseEnum.FAIL.message());
        return responseEntity;
    }

    public static <T> ResponseEntity<T> failBusinessException(int code, String message){
        ResponseEntity<T> responseEntity = new ResponseEntity<>();
        responseEntity.setMessage(message);
        responseEntity.setCode(code);
        return responseEntity;
    }
}

