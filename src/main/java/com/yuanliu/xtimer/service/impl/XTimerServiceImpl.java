package com.yuanliu.xtimer.service.impl;

import com.yuanliu.xtimer.redis.DistributeLock;
import com.yuanliu.xtimer.dto.TimerDTO;
import com.yuanliu.xtimer.enums.TimerStatus;
import com.yuanliu.xtimer.exception.BusinessException;
import com.yuanliu.xtimer.exception.ErrorCode;
import com.yuanliu.xtimer.manager.MigratorManager;
import com.yuanliu.xtimer.mapper.TimerMapper;
import com.yuanliu.xtimer.model.TimerModel;
import com.yuanliu.xtimer.service.XTimerService;
import com.yuanliu.xtimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.service.impl
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:54
 * @Version 1.0
 */
@Service
@Slf4j
public class XTimerServiceImpl implements XTimerService {

    @Autowired
    private TimerMapper timerMapper;

    @Autowired
    DistributeLock distributeLock;

    @Autowired
    MigratorManager migratorManager;

    private static final int defaultGapSeconds = 3;

    @Override
    public Long CreateTimer(TimerDTO timerDTO) {
        boolean isVaildCron = CronExpression.isValidExpression(timerDTO.getCron());
        if(!isVaildCron){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"cron表达式无效");
        }

        TimerModel timerModel = TimerModel.voToObj(timerDTO);
        if(timerModel == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数缺失");
        }

        timerMapper.save(timerModel);
        return timerModel.getTimerId();
    }

    @Override
    public void EnableTimer(String app, long id) { //timer_id
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = distributeLock.lock(
                TimerUtils.GetEnableLockKey(app), //key
                lockToken, //value
                defaultGapSeconds); //锁的过期时间
        if(!ok){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"激活操作过于频繁，请稍后再试！");
        }
        //执行真正地激活逻辑
        doEnableTimer(id);
    }

    @Transactional
    public void doEnableTimer(long id){
        //1.从数据库获取Timer
        TimerModel timerModel = timerMapper.getTimerById(id);
        if(timerModel == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"激活失败,timer不存在.timerId"+id);
        }
        //2.校验Timer的状态
        if(timerModel.getStatus() == TimerStatus.Enable.getStatus()){
            log.warn("Timer已经被激活了，无法重复激活,timerId:"+timerModel.getTimerId());
        }
        //3.修改timer状态为激活态
        timerModel.setStatus(TimerStatus.Enable.getStatus());
        timerMapper.update(timerModel);
        //4.迁移数据
        migratorManager.migrateTimer(timerModel);
    }
}
