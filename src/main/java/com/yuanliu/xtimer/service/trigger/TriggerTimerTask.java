package com.yuanliu.xtimer.service.trigger;

import com.yuanliu.xtimer.common.conf.TriggerAppConf;
import com.yuanliu.xtimer.enums.TaskStatus;
import com.yuanliu.xtimer.mapper.TaskMapper;
import com.yuanliu.xtimer.model.TaskModel;
import com.yuanliu.xtimer.redis.TaskCache;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.service.trigger
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/14 14:17
 * @Version 1.0
 */
@Slf4j
public class TriggerTimerTask extends TimerTask {
    TriggerAppConf triggerAppConf;

    TriggerPoolTask triggerPoolTask;

    TaskCache taskCache;

    TaskMapper taskMapper;

    private CountDownLatch latch;

    private Long count=0L;

    private Date startTime;

    private Date endTime;

    private String minuteBucketKey;

    public TriggerTimerTask(Date startTime, Date endTime, String minuteBucketKey,
                            CountDownLatch latch, TaskMapper taskMapper,
                            TaskCache taskCache, TriggerPoolTask triggerPoolTask,
                            TriggerAppConf triggerAppConf) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.minuteBucketKey = minuteBucketKey;
        this.latch = latch;
        this.taskMapper = taskMapper;
        this.taskCache = taskCache;
        this.triggerPoolTask = triggerPoolTask;
        this.triggerAppConf = triggerAppConf;
    }

    @Override
    public void run() {
        Date tStart = new Date(startTime.getTime()+count*triggerAppConf.getZrangeGapSeconds()*1000L);
        //推出条件： tStart >= endTime 的时候就应该退出了，表示执行完成
        //latch.countDown()就是告诉阻塞的主线程可以继续运行了
        if(tStart.compareTo(endTime)>=0){
            latch.countDown();
            return;
        }
        //处理1秒中内的任务
        try{
            handleBatch(tStart,new Date(tStart.getTime()+triggerAppConf.getZrangeGapSeconds()*1000L));
        }catch(Exception e){
            log.error("handleBatch Error. minuteBucketKey"+minuteBucketKey+",tStartTime"+tStart+",e:",e);
        }
        count++;
    }

    private void handleBatch(Date start, Date end){
        //获取待触发的任务
        List<TaskModel> tasks = getTasksByTime(start,end);
        if(Collections.isEmpty(tasks)){
            return;
        }
        for(TaskModel task:tasks){
            try{
                if(task==null)
                    continue;
                triggerPoolTask.runExecutor(task);
            }catch (Exception e){
                log.error("执行器执行任务失败,task"+task.toString());
            }
        }

    }
    private List<TaskModel> getTasksByTime(Date start,Date end){
        List<TaskModel> tasks = new ArrayList<>();
        //在获取这1s的任务时，都是左闭右开区间，也就是[start,end)
        //先走缓存
        try{
            tasks = taskCache.getTasksFromCache(minuteBucketKey, start.getTime(),end.getTime());
            if(!Collections.isEmpty(tasks)){
                return tasks;
            }
        }catch(Exception e){
            log.error("从Redis获取具体任务数据失败");
        }
        //没有从Redis处获得具体任务数据，改为从数据库处获取
        try{
            tasks = taskMapper.getTasksByTimeRange(start.getTime(),end.getTime(), TaskStatus.NotRun.getStatus());
        }catch(Exception e1){
            log.error("从MySQL中获取具体任务数据失败");
        }
        return tasks;
    }
}
