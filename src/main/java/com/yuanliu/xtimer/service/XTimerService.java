package com.yuanliu.xtimer.service;

import com.yuanliu.xtimer.dto.TimerDTO;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.service
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:52
 * @Version 1.0
 */
public interface XTimerService {
    Long CreateTimer(TimerDTO timerDTO);

    void EnableTimer(String app, long id);
}
