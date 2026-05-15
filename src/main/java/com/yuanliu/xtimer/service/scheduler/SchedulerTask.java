package com.yuanliu.xtimer.service.scheduler;

import com.yuanliu.xtimer.common.conf.SchedulerAppConf;
import com.yuanliu.xtimer.redis.DistributeLock;
import com.yuanliu.xtimer.service.trigger.TriggerWorker;
import com.yuanliu.xtimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.Date;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.service.scheduler
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/14 9:22
 * @Version 1.0
 */
@Component
@Slf4j
public class SchedulerTask {

    @Autowired
    DistributeLock distributeLock;

    @Autowired
    SchedulerAppConf schedulerAppConf;

    @Autowired
    TriggerWorker triggerWorker;

    @Async("schedulerPool")
    public void asyncHandleSlice(Date date, int bucketId){
        log.info("调度器开始对二维分片进行处理");

        //只加锁不解锁，只有超时才会解锁，锁住二维分片后的单个桶(分钟+bucketIndex)
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = distributeLock.lock(
                TimerUtils.GetTimeBucketLockKey(date,bucketId),
                lockToken,
                schedulerAppConf.getTryLockSeconds());
        if(!ok){
            log.info("调度器获取分布式锁失败");
            return;
        }

        log.info("调度器获取分布式锁成功，相应的key为 %s",TimerUtils.GetTimeBucketLockKey(date,bucketId));

        //在获取了相应的二维分片之后，将相应的二维分片交给触发器进行处理
        triggerWorker.work(TimerUtils.GetSliceMsgKey(date,bucketId));

        //延长分布式锁的时间，避免重复执行分片
        distributeLock.expireLock(
                TimerUtils.GetTimeBucketLockKey(date,bucketId),
                lockToken,
                schedulerAppConf.getSuccessExpireSeconds());
        log.info("调度器结束执行");
    }
}
