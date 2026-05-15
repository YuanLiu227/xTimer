package com.yuanliu.xtimer.utils;

import org.quartz.CronExpression;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.utils
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 13:45
 * @Version 1.0
 */
public class TimerUtils {

    public static String GetEnableLockKey(String app){
        return "enable_timer_lock_"+app;
    }

    public static String GetTokenStr(){
        long timestamp = System.currentTimeMillis();
        String thread = Thread.currentThread().getName();
        return thread+timestamp;
    }

    public static Date GetForwardTwoMigrateStepEnd(Date start,int diffMinutes){
        Date end = new Date(start.getTime()+2L*diffMinutes*60000);
        return end;
    }

    public static List<Long> GetCronNextsBetween(CronExpression cronExpression,Date now, Date end){
        List<Long> times = new ArrayList<>();
        if(end.before(now)){
            return times;
        }
        for(Date start = now ; start.before(end);){
            Date next = cronExpression.getNextValidTimeAfter(start);
            if(next == null || !next.before(end))
                break;
            times.add(next.getTime());
            start=next;
        }
        return times;
    }

    public static String UnionTimerIDUnix(long timerId, long unix){
        return new StringBuilder().append(timerId).append("_").append(unix).toString();
    }

    public static String GetTimeBucketLockKey(Date time,int bucketId){
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(time);
        return sb.append("time_bucket_lock_").append(timeStr).append("_").append(bucketId).toString();
    }

    public static String GetSliceMsgKey(Date time, int bucketId){
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(time);
        return sb.append(timeStr).append("_").append(bucketId).toString();
    }

    public static List<Long> SplitTimerIDUnix(String timerIDUnix){
        List<Long> longSet =  new ArrayList<>();
        String[] strList = timerIDUnix.split("_");
        if(strList.length!=2){
            return longSet;
        }
        longSet.add(Long.parseLong(strList[0]));
        longSet.add(Long.parseLong(strList[1]));
        return longSet;
    }

    public static String GetMigratorLockKey(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        String dateStr = sdf.format(date);
        return "migrator_lock_"+ dateStr;
    }
}
