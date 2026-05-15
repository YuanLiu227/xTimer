package com.yuanliu.xtimer.redis;

import com.yuanliu.xtimer.common.RedisBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;


/**
 * ClassName:
 * Package: com.yuanliu.xtimer.common
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 13:50
 * @Version 1.0
 */
@Component
@Slf4j
public class DistributeLock {
    @Autowired
    private RedisBase redisBase;
    public boolean lock(String key,String token,long expireSeconds){
        boolean ok = redisBase.setnx(key,token,expireSeconds);
        if(!ok){
            log.info("Failed to acquire lock");
        }
        return ok;
    }

    public void expireLock(String key, String token, long expireSeconds){
        Long execute = redisBase.executeLua(getExpireLockScript(), Arrays.asList(key),token,expireSeconds);
        if(execute.longValue() == 0){
            log.info("延期{}失败:{}",key,execute);
        }else if(execute.longValue() ==1){
            log.info("延期{}成功:{}",key,execute);
        }
    }

    private DefaultRedisScript<Long> getExpireLockScript(){
        String script = " local lockerKey = KEYS[1]\n"+
                " local targetToken = ARGV[1]\n"+
                " local duration = ARGV[2]\n"+
                " local getToken = redis.call('get',lockerKey)\n"+
                " if (not getToken or getToken ~= targetToken) then\n"+
                "\treturn 0\n"+
                "else\n"+
                "\treturn redis.call('expire',lockerKey,duration)\n"+
                "end";
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(Long.class);
        defaultRedisScript.setScriptText(script);
        return defaultRedisScript;
    }
}
