package com.yuanliu.xtimer.redis;

import com.yuanliu.xtimer.common.conf.SchedulerAppConf;
import com.yuanliu.xtimer.exception.BusinessException;
import com.yuanliu.xtimer.exception.ErrorCode;
import com.yuanliu.xtimer.model.TaskModel;
import com.yuanliu.xtimer.utils.TimerUtils;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.redis
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 16:21
 * @Version 1.0
 */
@Component
@Slf4j
public class TaskCache {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    SchedulerAppConf schedulerAppConf;
    public String GetTableName(TaskModel taskModel){
        int maxBucket = schedulerAppConf.getBucketsNum();

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(new Date(taskModel.getRunTimer()));
        long index = taskModel.getTimerId() % maxBucket;
        return sb.append(timeStr).append("_").append(index).toString();
    }
    public boolean cacheSaveTasks(List<TaskModel> taskList){
        try{
            SessionCallback sessionCallback = new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    for(TaskModel task:taskList) {
                        long unix = task.getRunTimer();
                        String tableName = GetTableName(task);
                        redisTemplate.opsForZSet().add(
                                tableName, //key
                                TimerUtils.UnionTimerIDUnix(task.getTimerId(),unix), //member
                                unix); //value
                    }
                    return operations.exec();
                }
            };
            redisTemplate.execute(sessionCallback);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<TaskModel> getTasksFromCache(String key, long start, long end){
        List<TaskModel> tasks =new ArrayList<>();

        Set<Object> timerIDUnixs = redisTemplate.opsForZSet().rangeByScore(key,start,end-1);
        if(Collections.isEmpty(timerIDUnixs)){
            return tasks;
        }

        for(Object timerIDUnixObj : timerIDUnixs){
            TaskModel task = new TaskModel();
            String timerIDUnix = (String) timerIDUnixObj;
            List<Long>  longSet = TimerUtils.SplitTimerIDUnix(timerIDUnix);
            if(longSet.size()!=2){
                log.error("splitTimerIDUnix 错误,timerIDUnix:" +timerIDUnix);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"splitTimerIDUnix 错误,timerIDUnix:" +timerIDUnix);
            }
            task.setTimerId(longSet.get(0));
            task.setRunTimer(longSet.get(1));
            tasks.add(task);
        }
        return tasks;
    }
}
