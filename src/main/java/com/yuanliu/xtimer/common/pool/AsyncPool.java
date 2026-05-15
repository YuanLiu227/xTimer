package com.yuanliu.xtimer.common.pool;

import com.yuanliu.xtimer.common.conf.SchedulerAppConf;
import com.yuanliu.xtimer.common.conf.TriggerAppConf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.common.pool
 * Description:
 * 在该项目中，虽然启动了调度器和触发器的线程池，
 * 但是调度器的线程池是为了让多个触发器线程同时处理多个二维分片，
 * 触发器的线程池是为了让多个执行器线程同时处理多个任务。
 * @Author Yuan Liu
 * @Create 2026/5/14 9:35
 * @Version 1.0
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncPool {
    @Autowired
    SchedulerAppConf schedulerAppConf;
    @Autowired
    TriggerAppConf triggerAppConf;

    @Bean(name = "schedulerPool")
    public Executor schedulerPoolExecutor(){
        log.info("开启调度器线程池");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(schedulerAppConf.getCorePoolSize());
        //配置最大线程数
        executor.setMaxPoolSize(schedulerAppConf.getMaxPoolSize());
        //配置队列大小
        executor.setQueueCapacity(schedulerAppConf.getQueueCapacity());
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix(schedulerAppConf.getNamePrefix());

        //rejection-policy:当pool已经达到max size的时候，如何处理新任务
        //CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean(name = "triggerPool")
    public Executor triggerPoolExecutor(){
        log.info("开启触发器线程池");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(triggerAppConf.getCorePoolSize());
        executor.setMaxPoolSize(triggerAppConf.getMaxPoolSize());
        executor.setQueueCapacity(triggerAppConf.getQueueCapacity());
        executor.setThreadNamePrefix(triggerAppConf.getNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
