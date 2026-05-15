package com.yuanliu.xtimer.utils;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.commons.lang3.StringUtils;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.utils
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:20
 * @Version 1.0
 */
public class JSONUtil {
    private static final ObjectMapper mapper;

    static{
        mapper=new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    /**
     *  将对象序列化为JSON字符串
     */
    public static String toJsonString(Object obj){
        if(obj == null)
            return null;
        String result=null;
        try{
            result = mapper.writeValueAsString(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将JSON字符串反序列化为对象
     */
    public static<T> T parseObject(String jsonStr, Class<T> clazz){
        if(StringUtils.isBlank(jsonStr) || clazz == null)
            return null;
        T t = null;
        try{
            t = mapper.readValue(jsonStr,clazz);
        }catch (Exception e){
            e.printStackTrace();
        }
        return t;
    }
}
