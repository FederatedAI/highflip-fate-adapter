/**
 * Copyright 2022 bejson.com
 */

package com.webank.ai.fate.client.form.job;

import com.baidu.highflip.core.entity.runtime.Job;
import com.baidu.highflip.core.entity.runtime.basic.Status;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webank.ai.fate.client.form.dsl.Dsl;
import com.webank.ai.fate.common.deserializer.JsonMapStringDeserializer;
import com.webank.ai.fate.common.deserializer.JsonStringDeserializer;
import lombok.Data;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class Jobs {

    private long apply_resource_time;

    private boolean cancel_signal;

    private String cancel_time;

    private int cores;

    private String create_date;

    private Date create_time;

    private String description;

    private Dsl dsl;

    private long elapsed;

    private String end_date;

    private int end_scheduling_updates;

    private Date end_time;

    private String engine_name;

    private String engine_type;

    private String initiator_party_id;

    private String initiator_role;

    private boolean is_initiator;

    private String job_id;

    private int memory;

    private String name;

    private List<Integer> partners;

    private int party_id;

    private int progress;

    private boolean ready_signal;

    private String ready_time;

    private int remaining_cores;

    private int remaining_memory;

    private boolean rerun_signal;

    private boolean resource_in_use;

    private long return_resource_time;

    @JsonDeserialize(using = JsonStringDeserializer.class)
    private String role;

    @JsonDeserialize(using = JsonMapStringDeserializer.class)
    private Map<String, String> roles;

    private Runtime_conf runtime_conf;

    private Runtime_conf_on_party runtime_conf_on_party;

    private String start_date;

    private Date start_time;

    private String status;

    private String status_code;

    private String tag;

    private Train_runtime_conf train_runtime_conf;

    private String update_date;

    private Date update_time;

    private User user;

    private String user_id;

    public static Job convertToEntity(Jobs response) {
        Job job = new Job();
//        job.setJobId();
        job.setJobName(response.getName());
        job.setDescription(response.getDescription());
        job.setCreateTime(response.getCreate_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        job.setUpdateTime(response.getUpdate_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        job.setFinishTime(response.getEnd_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
//        job.setGraph();
        job.setStatus(Status.valueOf(response.getStatus() + ""));
//        job.setMessage();
//        job.setIsDeleted();
//        job.setInputTasks();
//        job.setOutputTasks();
        job.setBingingId(response.getJob_id());
//        job.setBinding();
        return job;
    }
}