package com.yuanliu.xtimer.enums;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.enums
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 15:05
 * @Version 1.0
 */
public enum TimerStatus {
    Unable(1),
    Enable(2),;

    private TimerStatus(int status){
        this.status= status;
    }

    private int status;

    public int getStatus(){
        return this.status;
    }


}
