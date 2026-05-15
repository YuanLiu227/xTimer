package com.yuanliu.xtimer.service.scheduler;

import com.yuanliu.xtimer.common.conf.SchedulerAppConf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.theme.FixedThemeResolver;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.service.scheduler
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/14 9:05
 * @Version 1.0
 */
@Component
@Slf4j
public class SchedulerWorker {



    @Autowired
    SchedulerAppConf schedulerAppConf;
    @Autowired
    SchedulerTask schedulerTask;

    /**
     * 该方法每隔1s执行一次，也就是调度器每隔1s轮询一次Redis的Zset
     */
    @Scheduled(fixedRate = 1000)
    public void scheduledTask(){
        log.info("任务执行时间:"+ LocalDateTime.now());
        handleSlices();
    }

    private void handleSlices(){
        for(int i=0;i<schedulerAppConf.getBucketsNum();i++){
            handleSlice(i);
        }
    }

    private void handleSlice(int bucketId){
        Date now = new Date();
        Date nowPreMin = new Date(now.getTime()-60000);
        try{
            schedulerTask.asyncHandleSlice(nowPreMin,bucketId);
        }catch(Exception e){
            log.error("[handle slice] submit nowPreMin task failed,err:"+e);
        }
        try{
            schedulerTask.asyncHandleSlice(now,bucketId);
        }catch (Exception e){
            log.error("[handle slice] submit now task failed,err:"+e);
        }
    }
}
