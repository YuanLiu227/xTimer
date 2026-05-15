package com.yuanliu.xtimer.service.migrator;

import com.yuanliu.xtimer.common.conf.MigratorAppConf;
import com.yuanliu.xtimer.enums.TimerStatus;
import com.yuanliu.xtimer.manager.MigratorManager;
import com.yuanliu.xtimer.mapper.TimerMapper;
import com.yuanliu.xtimer.model.TimerModel;
import com.yuanliu.xtimer.redis.DistributeLock;
import com.yuanliu.xtimer.utils.TimerUtils;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.service.migrator
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/14 16:36
 * @Version 1.0
 */
@Component
@Slf4j
public class MigratorWorker {

    @Autowired
    TimerMapper timerMapper;
    @Autowired
    MigratorAppConf migratorAppConf;
    @Autowired
    MigratorManager migratorManager;
    @Autowired
    DistributeLock distributeLock;

    @Scheduled(fixedRate = 10*1000) //60*60*1000 每一个小时执行一次
    public void work(){
        log.info("开始迁移时间:"+ LocalDateTime.now());
        Date startHour = getStartHour(new Date());
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = distributeLock.lock(
                TimerUtils.GetMigratorLockKey(startHour),
                lockToken,
                60L*migratorAppConf.getMigrateTryLockMinutes());
        if(!ok){
            log.warn("migrator get lock failed!"+TimerUtils.GetMigratorLockKey(startHour));
            return;
        }
        //开始迁移
        migrate();
    }

    private Date getStartHour(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        try{
            return sdf.parse(sdf.format(date));
        }catch (ParseException e){
            throw new RuntimeException(e);
        }
    }

    private void migrate(){
        List<TimerModel> timers = timerMapper.getTimersByStatus(TimerStatus.Enable.getStatus());
        if(Collections.isEmpty(timers)){
            log.info("migrate timers is empty");
            return;
        }

        for(TimerModel timerModel:timers){
            migratorManager.migrateTimer(timerModel);
        }
    }

}
