package com.webank.ai.fate.client;


import com.webank.ai.fate.client.form.ResultForm;
import com.webank.ai.fate.client.form.job.JobData;
import com.webank.ai.fate.client.form.task.TaskData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


@Slf4j
@Data
@Disabled
public class FateClientTest {

    public static final String TEST_FLOW_URL = "http://172.16.153.108:9380";

    FateClient client = FateClient.connect(TEST_FLOW_URL);

//    @Test
//    public void testVersionGet() {
//        JsonResultForm result = getClient().version();
//        log.info("result = {}", result.getData().toPrettyString());
//    }

    @Test
    public void testJobList() {
        ResultForm<JobData> result = getClient().listJob(1, 10);
        log.info("result = {}", result);
    }

    @Test
    public void testTaskList() {
        ResultForm<TaskData> result = getClient().listTask(null, 1, 10);
        log.info("result = {}", result);
    }

    @Test
    public void testJobQuery() {
        ResultForm<?> result = getClient().jobQuery("202212011128410103660");
        log.info("result = {}", result.getData().toString());
    }
}
