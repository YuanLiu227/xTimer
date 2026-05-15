package com.yuanliu.xtimer.model;

import com.yuanliu.xtimer.common.BaseModel;

import java.io.Serializable;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.model
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:48
 * @Version 1.0
 */
public class TaskModel extends BaseModel implements Serializable {
    private Integer taskId;
    private String app;
    private Long timerId;
    private String output;
    private Long runTimer;
    private int costTime;
    private int status;

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Long getTimerId() {
        return timerId;
    }

    public void setTimerId(Long timerId) {
        this.timerId = timerId;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Long getRunTimer() {
        return runTimer;
    }

    public void setRunTimer(Long runTimer) {
        this.runTimer = runTimer;
    }

    public int getCostTime() {
        return costTime;
    }

    public void setCostTime(int costTime) {
        this.costTime = costTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
