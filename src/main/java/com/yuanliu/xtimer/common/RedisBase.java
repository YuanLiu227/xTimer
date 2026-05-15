package com.yuanliu.xtimer.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.common
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 13:53
 * @Version 1.0
 */
@Component
public class RedisBase {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 从Redis中获取数据
     * @param key
     * @return
     */
    public Object get(String key){
        return key==null?null:redisTemplate.opsForValue().get(key);
    }

    /**
     * 将数据放入到Redis中并且设置时间，
     * 时间time要大于0，如果time小于等于0，将设置为无限期
     */
    public boolean setnx(String key, Object value,long time){
        try{
            if(time<=0)
                return false;
            return redisTemplate.opsForValue().setIfAbsent(key,value,time,TimeUnit.SECONDS);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public long executeLua(RedisScript<Long> script, List<String> keys, String token,Long expire){
        try{
            Long execute = redisTemplate.execute(script,keys,token,expire);
            return execute;
        }catch(Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}
