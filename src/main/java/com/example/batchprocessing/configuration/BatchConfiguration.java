package com.example.batchprocessing.configuration;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.batchprocessing.batch.InactiveAccountIdsJobCompletionNotificationListener;
import com.example.batchprocessing.batch.InactiveAccountIdsWriter;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Bean
    @StepScope
    public ListItemReader<Long> inactiveAccountIdsReader(@Value("#{jobParameters['inactiveAccountIds']}")
                                                         String inactiveAccountIds) {
        List<Long> inactiveAccountIdsList = List.of(
                inactiveAccountIds.substring(1, inactiveAccountIds.length() - 1).split(","))
                .stream()
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return new ListItemReader<>(inactiveAccountIdsList);
    }

    @Bean
    public PassThroughItemProcessor<Long> inactiveAccountIdsProcessor() {
        return new PassThroughItemProcessor<>();
    }

    @Bean
    public Job inactiveAccountIdsJob(JobBuilderFactory jobBuilderFactory,
                                     InactiveAccountIdsJobCompletionNotificationListener listener,
                                     Step inactiveAccountsEntriesStep) {
        return jobBuilderFactory.get("inactiveAccountIdsJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(inactiveAccountsEntriesStep)
                .end()
                .build();
    }

    @Bean
    public Step inactiveAccountsEntriesStep(StepBuilderFactory stepBuilderFactory,
                                            ListItemReader<Long> reader,
                                            InactiveAccountIdsWriter writer,
                                            PassThroughItemProcessor<Long> processor,
                                            @Value("${batch.job.inactive.accounts.create.entries.batch.size}") int batch,
                                            @Value("${batch.job.inactive.accounts.create.entries.retry.limit}") int retries) {
        return stepBuilderFactory.get("inactiveAccountsEntriesStep")
                .<Long, Long>chunk(batch)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retryLimit(retries)
                .retry(RuntimeException.class)
                .build();
    }
}
