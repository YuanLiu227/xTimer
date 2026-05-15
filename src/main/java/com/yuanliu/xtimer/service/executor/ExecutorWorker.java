package com.yuanliu.xtimer.service.executor;

import com.yuanliu.xtimer.dto.NotifyHTTPParam;
import com.yuanliu.xtimer.dto.TimerDTO;
import com.yuanliu.xtimer.enums.TaskStatus;
import com.yuanliu.xtimer.enums.TimerStatus;
import com.yuanliu.xtimer.exception.BusinessException;
import com.yuanliu.xtimer.exception.ErrorCode;
import com.yuanliu.xtimer.mapper.TaskMapper;
import com.yuanliu.xtimer.mapper.TimerMapper;
import com.yuanliu.xtimer.model.TaskModel;
import com.yuanliu.xtimer.model.TimerModel;
import com.yuanliu.xtimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.service.executor
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/14 13:55
 * @Version 1.0
 */
@Component
@Slf4j
public class ExecutorWorker {

    @Autowired
    TaskMapper taskMapper;
    @Autowired
    TimerMapper timerMapper;
    public void work(String timerIDUnixKey){
        List<Long> longSet = TimerUtils.SplitTimerIDUnix(timerIDUnixKey);
        if(longSet.size()!=2){
            log.error("splitTimerIDUnix 错误, timerIDUnix:"+timerIDUnixKey);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"splitTimerIDUnix 错误, timerIDUnix:"+timerIDUnixKey);
        }
        Long timerId = longSet.get(0);
        Long unix = longSet.get(1);

//        //查询出任务，判断是否执行过了。避免重复执行
//        TaskModel task = taskMapper.getTaskByTimerIdUnix(timerId,unix);
//        if(task.getStatus() != TaskStatus.NotRun.getStatus()){
//            log.warn("重复执行任务: timerId"+timerId+",runtimer:"+unix);
//            return;
//        }

        TaskModel task = taskMapper.getTaskByTimerIdUnix(timerId,unix);
        if(task == null){
            log.warn("任务不存在：timerId="+timerId+",runTimer="+unix);
            return;
        }

        int affectRows = taskMapper.updateStatusByTaskIdAndStatus(task.getTaskId(),
                TaskStatus.NotRun.getStatus(),
                TaskStatus.Running.getStatus());

        if(affectRows!=1){
            log.warn("任务已经被其他线程抢占或者已经被执行: timerId="+timerId+",runTimer="+unix);
            return;
        }
        task.setStatus(TaskStatus.Running.getStatus());

        //执行回调
        executeAndPostProcess(task,timerId,unix);
    }

    private void executeAndPostProcess(TaskModel taskModel,Long timerId,Long unix){
        //查询 timerModel
        TimerModel timerModel = timerMapper.getTimerById(timerId);
        if(timerModel == null){
            log.error("执行回调任务失败，找不到对应的定时任务。timerId:"+timerId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"执行回调任务失败，找不到对应的定时任务。timerId:"+timerId);
        }

        //判断定时器是否还处于激活状态，如果此时处于去激活状态，那就不回调了（说明用户关闭了定时任务）
        if(timerModel.getStatus() != TimerStatus.Enable.getStatus()){
            log.warn("定时任务已经处于去激活状态。timerId:"+timerId);

            taskModel.setStatus(TaskStatus.Failed.getStatus());
            taskModel.setOutput("timer disabled");
            taskModel.setCostTime((int)(new Date().getTime()-taskModel.getRunTimer()));
            taskMapper.update(taskModel);

            return;
        }

        //误差时间计算 = 任务真正被回调之前 - 任务预期执行时间
        int gapTime = (int) (new Date().getTime() - taskModel.getRunTimer());
        taskModel.setCostTime(gapTime);

        //执行http回调，通知业务方执行任务
        ResponseEntity<String> resp = null;
        try{
            resp = executeTimerCallBack(timerModel);
        }catch(Exception e){
            log.error("执行回调失败，抛出异常e:"+e);
        }

        //后置处理，更新Timer的执行结果
        if(resp == null){
            taskModel.setStatus(TaskStatus.Failed.getStatus());
            taskModel.setOutput("resp is null");
        }else if(resp.getStatusCode().is2xxSuccessful()){
            taskModel.setStatus(TaskStatus.Succeed.getStatus());
            taskModel.setOutput(resp.toString());
        }else{
            taskModel.setStatus(TaskStatus.Failed.getStatus());
            taskModel.setOutput(resp.toString());
        }
        taskMapper.update(taskModel);
    }

    private ResponseEntity<String> executeTimerCallBack(TimerModel timerModel){
        TimerDTO timerDTO = TimerModel.objToVO(timerModel);
        NotifyHTTPParam httpParam = timerDTO.getNotifyHTTPParam();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> resp = null;
        switch (httpParam.getMethod()){
            case "POST":
                resp = restTemplate.postForEntity(httpParam.getUrl(),httpParam.getBody(),String.class);
                break;
            default:
                log.error("不支持的httpMethod");
                break;
        }
        HttpStatus statusCode = resp.getStatusCode();
        if(!statusCode.is2xxSuccessful()){
            log.error("http 回调失败："+resp);
        }
        return resp;
    }
}
