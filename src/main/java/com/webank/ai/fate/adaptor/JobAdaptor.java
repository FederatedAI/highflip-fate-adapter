package com.webank.ai.fate.adaptor;

import com.baidu.highflip.core.engine.HighFlipRuntime;
import com.baidu.highflip.core.entity.runtime.Job;
import com.baidu.highflip.core.entity.runtime.Task;
import com.baidu.highflip.core.entity.runtime.basic.Action;
import com.baidu.highflip.core.entity.runtime.basic.Status;
import com.webank.ai.fate.client.form.ResultForm;
import com.webank.ai.fate.client.form.job.Jobs;
import com.webank.ai.fate.client.form.job.QueryJob;
import com.webank.ai.fate.client.form.task.Tasks;
import com.webank.ai.fate.context.FateContext;
import com.webank.ai.fate.translator.DSLTranslator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
    public Job createJob(Job job) {

        DSLTranslator.FateDAG dag = getContext()
                .getTranslator()
                .translate(job.getGraph());

        String bindId = getContext()
                .getClient()
                .jobSubmit(dag.getDsl(), dag.getConf());

        job.setBingingId(bindId);
        return job;
    }

    @Override
    public Job updateJob(Job job) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasJob(Job job) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Status getJobStatus(Job job) {
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
    public void deleteJob(Job job) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Job controlJob(Job job, Action action) {
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
    public Job getJobByIndex(int index, Job job) {
        log.info("client get job by index");
        Jobs response = getContext().getClient().listJob(index, 1).getData().getJobs().get(0);
        return Jobs.convertToEntity(response);
    }

    @Override
    public Optional<Job> moreJob(Job job, HighFlipRuntime runtime) {
        return Optional.empty();
    }

    @Override
    public int getTaskCount(Job job) {
        return getContext().getClient().listTask(job.getBingingId(), 1, Integer.MAX_VALUE).getData().getCount();
    }

    @Override
    public List<Task> getTaskList(Job job, List<Task> task) {
        return getContext().getClient().listTask(job.getBingingId(), 1, Integer.MAX_VALUE).getData().getTasks().stream()
                .map(Tasks::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int getJobLogCount(Job job) {
        return 0;
    }

    @Override
    public Iterator<String> getJobLog(Job job, int offset, int limit) {
        return null;
    }
}
