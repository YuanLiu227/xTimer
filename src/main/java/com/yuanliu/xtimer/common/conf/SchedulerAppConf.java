package com.yuanliu.xtimer.common.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.common.conf
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 16:34
 * @Version 1.0
 */
@Component
public class SchedulerAppConf {

    @Value("5")
    private int bucketsNum;

    @Value("70")
    private int tryLockSeconds;

    @Value("130")
    private int successExpireSeconds;

    @Value("10")
    private int corePoolSize;

    @Value("100")
    private int maxPoolSize;

    @Value("99999")
    private int queueCapacity;

    @Value("scheduler-")
    private String namePrefix;

    public int getBucketsNum() {
        return bucketsNum;
    }

    public void setBucketsNum(int bucketsNum) {
        this.bucketsNum = bucketsNum;
    }

    public int getTryLockSeconds() {
        return tryLockSeconds;
    }

    public void setTryLockSeconds(int tryLockSeconds) {
        this.tryLockSeconds = tryLockSeconds;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public int getSuccessExpireSeconds() {
        return successExpireSeconds;
    }

    public void setSuccessExpireSeconds(int successExpireSeconds) {
        this.successExpireSeconds = successExpireSeconds;
    }
}
