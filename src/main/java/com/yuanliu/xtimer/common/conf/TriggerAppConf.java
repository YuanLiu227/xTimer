package com.yuanliu.xtimer.common.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.common.conf
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/14 9:11
 * @Version 1.0
 */
@Component
public class TriggerAppConf {

    @Value("1")
    private int zrangeGapSeconds;

    @Value("10")
    private int corePoolSize;

    @Value("100")
    private int maxPoolSize;

    @Value("99999")
    private int queueCapacity;

    @Value("trigger-")
    private String namePrefix;

    public int getZrangeGapSeconds() {
        return zrangeGapSeconds;
    }

    public void setZrangeGapSeconds(int zrangeGapSeconds) {
        this.zrangeGapSeconds = zrangeGapSeconds;
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
}
