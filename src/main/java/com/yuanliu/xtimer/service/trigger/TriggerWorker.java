package com.yuanliu.xtimer.service.trigger;

import com.mchange.v2.cfg.PropertiesConfigSource;
import com.yuanliu.xtimer.common.conf.TriggerAppConf;
import com.yuanliu.xtimer.mapper.TaskMapper;
import com.yuanliu.xtimer.redis.TaskCache;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.service.trigger
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/14 10:15
 * @Version 1.0
 */
@Component
@Slf4j
public class TriggerWorker {
    @Autowired
    TriggerAppConf triggerAppConf;
    @Autowired
    TriggerPoolTask triggerPoolTask;
    @Autowired
    TaskCache taskCache;
    @Autowired
    TaskMapper taskMapper;

    public void work(String minuteBucketKey){
        //通过二维分片的timer_id+时间戳获得开始时间
        Date startTime = getStartMinute(minuteBucketKey);
        Date endTime = new Date(startTime.getTime()+60000);

        CountDownLatch latch = new CountDownLatch(1);
        Timer timer = new Timer("Timer");
        TriggerTimerTask task = new TriggerTimerTask(
                startTime,endTime,minuteBucketKey,latch,taskMapper,taskCache,triggerPoolTask,triggerAppConf);
        timer.scheduleAtFixedRate(task,0L,triggerAppConf.getZrangeGapSeconds()*1000L);
        try{
            latch.await();
        }catch(InterruptedException e){
            log.error("执行TriggerTimerTask异常中断,task:"+task);
        }finally {
            timer.cancel();
        }
    }

    private Date getStartMinute(String minuteBukcetKey){
        String[] timeBucket = minuteBukcetKey.split("_");
        if(timeBucket.length !=2){
            log.error("触发器获取开始时间错误");
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startMinute = null;
        try{
            startMinute = sdf.parse(timeBucket[0]);
        }catch(ParseException e){
            log.error("触发器获取开始时间错误");
        }
        return startMinute;
    }
}
