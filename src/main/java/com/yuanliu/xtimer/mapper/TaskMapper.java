package com.yuanliu.xtimer.mapper;

import com.yuanliu.xtimer.model.TaskModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.mapper
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/13 16:05
 * @Version 1.0
 */
@Mapper
public interface TaskMapper {

    void batchSave(@Param("taskList") List<TaskModel> taskList);

    List<TaskModel> getTasksByTimeRange(@Param("startTime") Long startTime,@Param("endTime") Long endTime, @Param("taskStatus") int taskStatus);

    TaskModel getTaskByTimerIdUnix(@Param("timerId") Long timerId,@Param("runTimer") Long runTimer);

    void update(@Param("taskModel") TaskModel taskModel);

    int updateStatusByTaskIdAndStatus(@Param("taskId") Integer taskId,
                                      @Param("fromStatus")Integer fromStatus,
                                      @Param("toStatus")Integer toStatus);
}
