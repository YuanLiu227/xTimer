package com.yuanliu.xtimer.model;

import com.yuanliu.xtimer.common.BaseModel;
import com.yuanliu.xtimer.dto.NotifyHTTPParam;
import com.yuanliu.xtimer.dto.TimerDTO;
import com.yuanliu.xtimer.utils.JSONUtil;

import java.io.Serializable;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.model
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:10
 * @Version 1.0
 */
public class TimerModel extends BaseModel implements Serializable {
    private Long timerId;
    private String app;
    private String name;
    private int status;
    private String cron;
    private String notifyHTTPParam;

    /**
     *  包装类转对象
     */
    public static TimerModel voToObj(TimerDTO timerDTO){
        if(timerDTO == null){
            return null;
        }
        TimerModel timerModel = new TimerModel();
        timerModel.setApp(timerDTO.getApp());
        timerModel.setTimerId(timerDTO.getTimerId());
        timerModel.setName(timerDTO.getName());
        timerModel.setStatus(timerDTO.getStatus());
        timerModel.setCron(timerDTO.getCron());
        timerModel.setNotifyHTTPParam(JSONUtil.toJsonString(timerDTO.getNotifyHTTPParam()));
        return timerModel;
    }

    /**
     * 对象转包装类
     * @return
     */
    public static TimerDTO objToVO(TimerModel timerModel){
        if(timerModel == null)
            return null;
        TimerDTO timerDTO = new TimerDTO();
        timerDTO.setApp(timerModel.getApp());
        timerDTO.setTimerId(timerModel.getTimerId());
        timerDTO.setName(timerModel.getName());
        timerDTO.setStatus(timerModel.getStatus());
        timerDTO.setCron(timerModel.getCron());

        NotifyHTTPParam httpParam = JSONUtil.parseObject(timerModel.getNotifyHTTPParam(), NotifyHTTPParam.class);
        timerDTO.setNotifyHTTPParam(httpParam);

        return timerDTO;
    }

    public Long getTimerId() {
        return timerId;
    }

    public void setTimerId(Long timerId) {
        this.timerId = timerId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getNotifyHTTPParam() {
        return notifyHTTPParam;
    }

    public void setNotifyHTTPParam(String notifyHTTPParam) {
        this.notifyHTTPParam = notifyHTTPParam;
    }
}
