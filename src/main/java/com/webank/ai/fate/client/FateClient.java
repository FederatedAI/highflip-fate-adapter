package com.webank.ai.fate.client;

import com.webank.ai.fate.client.form.ResultForm;
import com.webank.ai.fate.client.form.data.Data;
import com.webank.ai.fate.client.form.data.DataId;
import com.webank.ai.fate.client.form.dsl.Dsl;
import com.webank.ai.fate.client.form.dsl.DslConf;
import com.webank.ai.fate.client.form.job.FateJob;
import com.webank.ai.fate.client.form.job.JobData;
import com.webank.ai.fate.client.form.job.QueryJob;
import com.webank.ai.fate.client.form.task.TaskData;
import com.webank.ai.fate.common.deserializer.FeignSpringFormEncoder;
import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.Param;
import feign.Request;
import feign.RequestLine;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface FateClient {

    static FateClient connect(String url) {
        return Feign.builder().client(new ApacheHttpClient()).decoder(new JacksonDecoder())
                .encoder(new FeignSpringFormEncoder()).logLevel(Logger.Level.NONE)
                .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true))
                .target(FateClient.class, url);
    }

    @RequestLine("POST /v1/job/submit")
    @Headers("Content-Type: application/json")
    ResultForm<FateJob> jobSubmit(@Param("job_dsl") Dsl dsl, @Param("job_runtime_conf") DslConf conf);

    @RequestLine("POST /v1/job/stop")
    @Headers("Content-Type: application/json")
    void jobStop(@Param("job_id") String jobId);

    @RequestLine("POST /v1/job/query")
    @Headers("Content-Type: application/json")
    ResultForm<List<QueryJob>> jobQuery(@Param("job_id") String jobId);

    @RequestLine("POST /v1/job/list/job")
    @Headers("Content-Type: application/json")
    ResultForm<JobData> listJob(@Param("page") int page, @Param("limit") int limit);

    @RequestLine("POST /v1/job/task/query")
    @Headers("Content-Type: application/json")
    ResultForm<List<QueryJob>> taskQuery(@Param("task_id") String taskId);

    @RequestLine("POST /v1/job/list/task")
    @Headers("Content-Type: application/json")
    ResultForm<TaskData> listTask(@Param("job_id") @Nullable String jobId, @Param("page") int page,
            @Param("limit") int limit);

    @RequestLine("POST /v1/table/delete")
    @Headers("Content-Type: application/json")
    ResultForm<DataId> deleteData(@Param(value = "table_name") String tableName,
            @Param(value = "namespace") String nameSpace);

    @RequestLine("GET /v1/table/download")
    @Headers("Content-Type: application/json")
    Response downloadData(@Param(value = "name") String tableName, @Param(value = "namespace") String nameSpace);

    @RequestLine("POST /v1/data/upload")
    @Headers("Content-Type: application/octet-stream")
    ResultForm<Data> pushData(@Param(value = "file") MultipartFile file,
            @Param(value = "id_delimiter") String id_delimiter, @Param(value = "head") String head,
            @Param(value = "partition") String partition, @Param(value = "table_name") String table_name,
            @Param(value = "namespace") String namespace, @Param(value = "storage_engine") String storage_engine,
            @Param(value = "destory") String destory, @Param(value = "extend_sid") String extend_sid);

}
