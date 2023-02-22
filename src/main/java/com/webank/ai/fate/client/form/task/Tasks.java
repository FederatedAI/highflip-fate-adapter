
package com.webank.ai.fate.client.form.task;
import com.baidu.highflip.core.entity.runtime.Task;
import lombok.Data;

import java.time.ZoneId;
import java.util.List;
import java.util.Date;


@Data
public class Tasks {

    private int auto_retries;
    private int auto_retry_delay;
    private List<String> cmd;
    private String component_module;
    private String component_name;
    private Component_parameters component_parameters;
    private Date create_time;
    private int elapsed;
    private Date end_time;
    private Engine_conf engine_conf;
    private String federated_mode;
    private String federated_status_collect_type;
    private String initiator_party_id;
    private String initiator_role;
    private String job_id;
    private String party_id;
    private String party_status;
    private Provider_info provider_info;
    private String role;
    private String run_ip;
    private boolean run_on_this_party;
    private int run_pid;
    private Date start_time;
    private String status;
    private String status_code;
    private String task_id;
    private int task_version;
    private Date update_time;
    private String worker_id;

    public static Task convertToEntity(Tasks data) {
        Task task = new Task();
//        task.setTaskid();
        task.setJobid(data.getJob_id());
        task.setName(data.getComponent_name());
//        task.setDescription();
        task.setCreateTime(data.getCreate_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        task.setUpdateTime(data.getUpdate_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        task.setFinishTime(data.getEnd_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        task.setNodeName(data.getComponent_name());
        task.setStatus(data.getStatus());
//        task.setMessage();
//        task.setIsDeleted();
//        task.setPrevious();
//        task.setNext();
        task.setBingingId(data.getTask_id());
//        task.setBinding();
        return task;
    }

}