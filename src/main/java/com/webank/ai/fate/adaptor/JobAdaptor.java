package com.webank.ai.fate.adaptor;

import com.baidu.highflip.core.engine.HighFlipRuntime;
import com.baidu.highflip.core.entity.runtime.Task;
import com.baidu.highflip.core.entity.runtime.basic.Action;
import com.baidu.highflip.core.entity.runtime.basic.Status;
import com.webank.ai.fate.client.form.ResultForm;
import com.webank.ai.fate.client.form.job.FateJob;
import com.webank.ai.fate.client.form.job.QueryJob;
import com.webank.ai.fate.client.form.task.FateTask;
import com.webank.ai.fate.context.FateContext;
import com.webank.ai.fate.translator.DSLTranslator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class JobAdaptor implements com.baidu.highflip.core.adaptor.JobAdaptor {

    FateContext context;

    public JobAdaptor(FateContext context) {
        this.context = context;
    }

    @Override
    public List<String> getFeatures() {
        throw new UnsupportedOperationException();
    }

    @Override
    public com.baidu.highflip.core.entity.runtime.Job createJob(com.baidu.highflip.core.entity.runtime.Job job) {

        DSLTranslator.FateDAG dag = getContext()
                .getTranslator()
                .translate(job.getGraph());

        String bindId = getContext()
                .getClient()
                .jobSubmit(dag.getDsl(), dag.getConf()).getData().getJob_id();

        job.setBingingId(bindId);
        return job;
    }

    @Override
    public com.baidu.highflip.core.entity.runtime.Job updateJob(com.baidu.highflip.core.entity.runtime.Job job) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasJob(com.baidu.highflip.core.entity.runtime.Job job) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Status getJobStatus(com.baidu.highflip.core.entity.runtime.Job job) {
        String bindId = job.getBingingId();
        ResultForm<List<QueryJob>> result = getContext()
                .getClient()
                .jobQuery(bindId);

        String status = result.getData()
                .get(0)
                .getF_status();

        return Status.valueOf(status);
    }

    @Override
    public void deleteJob(com.baidu.highflip.core.entity.runtime.Job job) {
        throw new UnsupportedOperationException();
    }

    @Override
    public com.baidu.highflip.core.entity.runtime.Job controlJob(com.baidu.highflip.core.entity.runtime.Job job, Action action) {
        switch (action) {
            case STOP:
                getContext().getClient().jobStop(job.getBingingId());
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return null;
    }

    @Override
    public int getJobCount() {
        log.info("client get job count");
        return getContext().getClient().listJob(1, Integer.MAX_VALUE).getData().getCount();
    }

    @Override
    public com.baidu.highflip.core.entity.runtime.Job getJobByIndex(int index, com.baidu.highflip.core.entity.runtime.Job job) {
        log.info("client get job by index");
        FateJob response = getContext().getClient().listJob(index + 1, 1).getData().getJobs().get(0);
        return FateJob.convertToEntity(response);
    }

    @Override
    public Optional<com.baidu.highflip.core.entity.runtime.Job> moreJob(com.baidu.highflip.core.entity.runtime.Job job, HighFlipRuntime runtime) {
        return Optional.empty();
    }

    @Override
    public int getTaskCount(com.baidu.highflip.core.entity.runtime.Job job) {
        return getContext().getClient().listTask(job.getBingingId(), 1, Integer.MAX_VALUE).getData().getCount();
    }

    @Override
    public List<Task> getTaskList(com.baidu.highflip.core.entity.runtime.Job job, List<Task> tasks) {
        List<Task> queryResult = getContext().getClient().listTask(job.getBingingId(), 1, Integer.MAX_VALUE).getData().getTasks().stream()
                .map(FateTask::convertToEntity)
                .collect(Collectors.toList());
        for (int i = 0; i < tasks.size(); i++) {
            Task queryTask = queryResult.get(i);
            Task ret = tasks.get(i);
            queryTask.setTaskid(ret.getTaskid());
            BeanUtils.copyProperties(queryTask, ret);
        }
        return tasks;
    }

    @Override
    public int getJobLogCount(com.baidu.highflip.core.entity.runtime.Job job) {
        return 0;
    }

    @Override
    public Iterator<String> getJobLog(com.baidu.highflip.core.entity.runtime.Job job, int offset, int limit) {
        return null;
    }
}
