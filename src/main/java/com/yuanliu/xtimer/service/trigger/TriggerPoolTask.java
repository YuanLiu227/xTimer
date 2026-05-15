package com.yuanliu.xtimer.service.trigger;

import com.yuanliu.xtimer.model.TaskModel;
import com.yuanliu.xtimer.service.executor.ExecutorWorker;
import com.yuanliu.xtimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.service.trigger
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/14 13:55
 * @Version 1.0
 */
@Component
@Slf4j
public class TriggerPoolTask {

    @Autowired
    ExecutorWorker executorWorker;

    @Async("triggerPool")
    public void runExecutor(TaskModel task){
        if(task == null)
            return;
        log.info("执行器开始执行任务");
        executorWorker.work(TimerUtils.UnionTimerIDUnix(task.getTimerId(),task.getRunTimer()));
        log.info("执行器结束执行任务");
    }
}
