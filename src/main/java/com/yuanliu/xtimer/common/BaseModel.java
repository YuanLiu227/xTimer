package com.yuanliu.xtimer.common;

import java.io.Serializable;
import java.util.Date;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.model
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:07
 * @Version 1.0
 */
public class BaseModel implements Serializable {
    public Date createTime;
    public Date modifyTime;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public String toString() {
        return "BaseModel{" +
                "createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                '}';
    }
}
