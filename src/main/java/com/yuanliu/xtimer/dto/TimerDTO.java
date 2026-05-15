package com.yuanliu.xtimer.dto;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.dto
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:13
 * @Version 1.0
 */
public class TimerDTO {
    private Long timerId;
    private String app;
    private String name;
    private int status;
    private String cron;
    private NotifyHTTPParam notifyHTTPParam;

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

    public NotifyHTTPParam getNotifyHTTPParam() {
        return notifyHTTPParam;
    }

    public void setNotifyHTTPParam(NotifyHTTPParam notifyHTTPParam) {
        this.notifyHTTPParam = notifyHTTPParam;
    }
}
