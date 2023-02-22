package com.webank.ai.fate.client;

import com.webank.ai.fate.client.form.ResultForm;
import com.webank.ai.fate.client.form.dsl.Dsl;
import com.webank.ai.fate.client.form.dsl.DslConf;
import com.webank.ai.fate.client.form.job.JobData;
import com.webank.ai.fate.client.form.job.QueryJob;
import com.webank.ai.fate.client.form.task.TaskData;
import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.Request;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Headers("Content-Type: application/json")
public interface FateClient {

    static FateClient connect(String url) {
        return Feign.builder()
                .decoder(new JacksonDecoder())
                .encoder(new JacksonEncoder())
                .options(new Request.Options(
                        10, TimeUnit.SECONDS,
                        60, TimeUnit.SECONDS,
                        true))
                .target(FateClient.class, url);
    }

    @RequestLine("POST /v1/job/submit")
    String jobSubmit(@Param("job_dsl") Dsl dsl,
                     @Param("job_runtime_conf") DslConf conf);

    @RequestLine("POST /v1/job/stop")
    void jobStop(@Param("job_id") String jobId);

    @RequestLine("POST /v1/job/query")
    ResultForm<List<QueryJob>> jobQuery(@Param("job_id") String jobId);

    @RequestLine("POST /v1/job/list/job")
    ResultForm<JobData> listJob(@Param("page") int page, @Param("limit") int limit);

    @RequestLine("POST /v1/job/task/query")
    ResultForm<List<QueryJob>> taskQuery(@Param("task_id") String taskId);

    @RequestLine("POST /v1/job/list/task")
    ResultForm<TaskData> listTask(@Param("job_id") String jobId, @Param("page") int page, @Param("limit") int limit);

}
