package com.yuanliu.xtimer.mapper;

import com.yuanliu.xtimer.model.TimerModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.mapper
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 15:33
 * @Version 1.0
 */
@Mapper
public interface TimerMapper {

    void save(@Param("timerModel") TimerModel timerModel);

    TimerModel getTimerById(@Param("timerId") Long timerId);

    void update(@Param("timerModel") TimerModel timerModel);

    List<TimerModel> getTimersByStatus(@Param("status") int status);
}
