package com.yuanliu.xtimer.manager;

import com.yuanliu.xtimer.common.conf.MigratorAppConf;
import com.yuanliu.xtimer.enums.TaskStatus;
import com.yuanliu.xtimer.enums.TimerStatus;
import com.yuanliu.xtimer.exception.BusinessException;
import com.yuanliu.xtimer.exception.ErrorCode;
import com.yuanliu.xtimer.mapper.TaskMapper;
import com.yuanliu.xtimer.model.TaskModel;
import com.yuanliu.xtimer.model.TimerModel;
import com.yuanliu.xtimer.redis.TaskCache;
import com.yuanliu.xtimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.manager
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 15:20
 * @Version 1.0
 */
@Service
@Slf4j
public class MigrateManagerImpl implements MigratorManager{

    @Autowired
    private MigratorAppConf migratorAppConf;
    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskCache taskCache;

    @Override
    public void migrateTimer(TimerModel timerModel) {
        //判断当前的timer是否处于激活状态，只有处于激活状态的任务才可以被迁移
        if(timerModel.getStatus() != TimerStatus.Enable.getStatus()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Timer不处于激活状态,迁移失败,timerId:"+timerModel.getTimerId());
        }

        //取得未来一段时间内任务执行的具体时间，现在的设计是2个小时
        CronExpression cronExpression;
        try{
            cronExpression = new CronExpression(timerModel.getCron());
        }catch (ParseException e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析cron表达式失败:"+timerModel.getCron());
        }
        Date now = new Date();
        Date end = TimerUtils.GetForwardTwoMigrateStepEnd(now,migratorAppConf.getMigrateStepMinutes());
        List<Long> executeTimes = TimerUtils.GetCronNextsBetween(cronExpression,now,end);
        if(CollectionUtils.isEmpty(executeTimes)){
            log.warn("获取到的任务的具体执行时间列表为空");
            return;
        }

        //将timer中的定时任务构造成具体任务，然后迁移到timer_task数据库中
        List<TaskModel> taskList = batchTasksFromTimer(timerModel,executeTimes);
        // 基于timer_id(定时任务id) + run_timer(任务具体执行时间) 唯一键，包装任务不被重复插入
        taskMapper.batchSave(taskList);

        //将具体任务缓存到Redis
        boolean cacheRes = taskCache.cacheSaveTasks(taskList);
        if(!cacheRes){
            log.error("Zset存储具体任务失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Zset存储具体任务失败，timerId:"+timerModel.getTimerId());
        }

    }

    private List<TaskModel> batchTasksFromTimer(TimerModel timerModel, List<Long> executeTimes){
        if(timerModel == null || CollectionUtils.isEmpty(executeTimes)){
            return null;
        }
        List<TaskModel> taskList = new ArrayList<>();
        for(long runTimer:executeTimes){
            TaskModel task = new TaskModel();
            task.setApp(timerModel.getApp());
            task.setTimerId(timerModel.getTimerId());
            task.setRunTimer(runTimer);
            task.setStatus(TaskStatus.NotRun.getStatus());
            taskList.add(task);
        }
        return taskList;
    }
}

