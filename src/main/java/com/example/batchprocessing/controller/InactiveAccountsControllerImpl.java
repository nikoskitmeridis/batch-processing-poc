package com.example.batchprocessing.controller;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.RestController;
import com.example.batchprocessing.controller.dto.InactiveAccountIdsDto;

@RestController
public class InactiveAccountsControllerImpl implements InactiveAccountsController {

    private final JobLauncher jobLauncher;
    private final Job inactiveAccountIdsJob;

    public InactiveAccountsControllerImpl(JobLauncher jobLauncher, Job inactiveAccountIdsJob) {

        this.jobLauncher = jobLauncher;
        this.inactiveAccountIdsJob = inactiveAccountIdsJob;
    }

    @Override
    public Callable<Void> createEntries(InactiveAccountIdsDto inactiveAccountIdsDto) {
        return () -> {
            List<Long> inactiveAccountIds = inactiveAccountIdsDto.getInactiveAccountIds();
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addDate("date", new Date());
            jobParametersBuilder.addString("inactiveAccountIds", inactiveAccountIds.toString());
            jobLauncher.run(inactiveAccountIdsJob, jobParametersBuilder.toJobParameters());
            return null;
        };
    }
}
