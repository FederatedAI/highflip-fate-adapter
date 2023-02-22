package com.webank.ai.fate.client.form;

import com.baidu.highflip.core.entity.runtime.Task;
import lombok.Data;

import java.time.ZoneId;
import java.util.Date;

@Data
public class TaskResponse {

    String task_id;

    String job_id;

    String description;

    Date create_time;

    Date update_time;

    Date end_time;

    String status;

    public static Task convertToEntity(TaskResponse taskResponse) {
        Task task = new Task();
        task.setBingingId(taskResponse.getTask_id());
        task.setJobid(taskResponse.getJob_id());
        task.setCreateTime(taskResponse.getCreate_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        task.setUpdateTime(taskResponse.getUpdate_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        task.setFinishTime(taskResponse.getEnd_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        task.setStatus(taskResponse.getStatus());
        return task;
    }

}
