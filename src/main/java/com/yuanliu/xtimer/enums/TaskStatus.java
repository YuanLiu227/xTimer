package com.yuanliu.xtimer.enums;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.enums
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 16:01
 * @Version 1.0
 */
public enum TaskStatus {
    NotRun(0),
    Running(1),
    Succeed(2),
    Failed(3);

    private TaskStatus(int status){
        this.status = status;
    }
    private int status;

    public int getStatus() {
        return status;
    }
}
