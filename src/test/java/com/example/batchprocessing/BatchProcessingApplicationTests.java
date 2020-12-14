package com.example.batchprocessing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.batchprocessing.controller.dto.InactiveAccountIdsDto;
import com.example.batchprocessing.domain.operation.Operation;
import com.example.batchprocessing.domain.operation.OperationStatus;
import com.example.batchprocessing.domain.operation.OperationType;
import com.example.batchprocessing.domain.operation.inactive.accounts.InactiveAccountsOperationPayload;
import com.example.batchprocessing.domain.task.Task;
import com.example.batchprocessing.domain.task.TaskResult;
import com.example.batchprocessing.domain.task.TaskStatus;
import com.example.batchprocessing.domain.task.TaskType;
import com.example.batchprocessing.domain.task.inactive.accounts.InactiveAccountsTaskPayload;
import com.example.batchprocessing.repository.AccountRepository;
import com.example.batchprocessing.repository.OperationRepository;
import com.example.batchprocessing.repository.TaskRepository;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@SpringBatchTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BatchProcessingApplicationTests {

    private static final String IMAGE_VERSION = "postgres:10.2-alpine";

    @Container
    private static JdbcDatabaseContainer container = new PostgreSQLContainer<>(IMAGE_VERSION)
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("root")
            .withInitScript("test-init.sql")
            .withCreateContainerCmdModifier(e -> e.withHostConfig(new HostConfig().withPortBindings(
                    new Ports(new ExposedPort(5432), Ports.Binding.bindPort(5433)))));

    @LocalServerPort
    private int port;

    @MockBean
    private PassThroughItemProcessor<Long> processor;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private OperationRepository operationRepository;

    @BeforeEach
    public void setup() throws Exception {
        assertTrue(container.isRunning());
        when(processor.process(anyLong())).thenCallRealMethod();
    }

    @Test
    @Order(1)
    void httpTest() throws URISyntaxException {
        URI uri = new URI("http://localhost:" + port + "/inactive-accounts/create-entries");
        InactiveAccountIdsDto requestBody = new InactiveAccountIdsDto(List.of(1L, 2L, 3L, 4L, 5L));
        HttpEntity<InactiveAccountIdsDto> request = new HttpEntity<>(requestBody, null);

        ResponseEntity<Void> result = this.restTemplate.postForEntity(uri, request, Void.class);
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    @Order(2)
    void databaseTest() throws URISyntaxException {
        assertEquals(10, accountRepository.count());
        assertEquals(5, taskRepository.count());
        assertEquals(5, operationRepository.count());

        List<Operation> expectedOperations = createExpectedOperations();
        List<Operation> actualOperations = operationRepository.findAll();
        expectedOperations.forEach(expectedOperation ->
                assertTrue(actualOperations.stream().anyMatch(actualOperation ->
                        actualOperation.getPayload().equals(expectedOperation.getPayload()) &&
                                actualOperation.getStatus() == expectedOperation.getStatus() &&
                                actualOperation.getType() == expectedOperation.getType())));

        List<Task> expectedTasks = createExpectedTasks(expectedOperations);
        List<Task> actualTasks = taskRepository.findAll();
        expectedTasks.forEach(expectedTask ->
                assertTrue(actualTasks.stream().anyMatch(actualTask ->
                        actualTask.getPayload().equals(expectedTask.getPayload()) &&
                                actualTask.getStatus() == expectedTask.getStatus() &&
                                actualTask.getType() == expectedTask.getType() &&
                                actualTask.getResult() == expectedTask.getResult() &&
                                actualTask.getOperation().getType() == expectedTask.getOperation().getType() &&
                                actualTask.getOperation().getStatus() == expectedTask.getOperation().getStatus() &&
                                actualTask.getOperation().getPayload().equals(expectedTask.getOperation().getPayload()))));
    }

    private List<Operation> createExpectedOperations() {
        Operation expectedOperation1 = new Operation(
                new InactiveAccountsOperationPayload(1L, "nikos@email.com"),
                OperationType.TERMINATE_ACCOUNT,
                OperationStatus.PENDING);
        Operation expectedOperation2 = new Operation(
                new InactiveAccountsOperationPayload(2L, "nikos2@email.com"),
                OperationType.TERMINATE_ACCOUNT,
                OperationStatus.PENDING);
        Operation expectedOperation3 = new Operation(
                new InactiveAccountsOperationPayload(3L, "nikos3@email.com"),
                OperationType.TERMINATE_ACCOUNT,
                OperationStatus.PENDING);
        Operation expectedOperation4 = new Operation(
                new InactiveAccountsOperationPayload(4L, "nikos4@email.com"),
                OperationType.TERMINATE_ACCOUNT,
                OperationStatus.PENDING);
        Operation expectedOperation5 = new Operation(
                new InactiveAccountsOperationPayload(5L, "nikos5@email.com"),
                OperationType.TERMINATE_ACCOUNT,
                OperationStatus.PENDING);
        return List.of(
                expectedOperation1,
                expectedOperation2,
                expectedOperation3,
                expectedOperation4,
                expectedOperation5);
    }

    private List<Task> createExpectedTasks(List<Operation> expectedOperations) {
        Task expectedTask1 = new Task(new InactiveAccountsTaskPayload(1L),
                TaskType.TERMINATE_ACCOUNT,
                TaskStatus.PENDING,
                TaskResult.NOT_STARTED,
                expectedOperations.get(0));
        Task expectedTask2 = new Task(new InactiveAccountsTaskPayload(2L),
                TaskType.TERMINATE_ACCOUNT,
                TaskStatus.PENDING,
                TaskResult.NOT_STARTED,
                expectedOperations.get(1));
        Task expectedTask3 = new Task(new InactiveAccountsTaskPayload(3L),
                TaskType.TERMINATE_ACCOUNT,
                TaskStatus.PENDING,
                TaskResult.NOT_STARTED,
                expectedOperations.get(2));
        Task expectedTask4 = new Task(new InactiveAccountsTaskPayload(4L),
                TaskType.TERMINATE_ACCOUNT,
                TaskStatus.PENDING,
                TaskResult.NOT_STARTED,
                expectedOperations.get(3));
        Task expectedTask5 = new Task(new InactiveAccountsTaskPayload(5L),
                TaskType.TERMINATE_ACCOUNT,
                TaskStatus.PENDING,
                TaskResult.NOT_STARTED,
                expectedOperations.get(4));
        return List.of(expectedTask1, expectedTask2, expectedTask3, expectedTask4, expectedTask5);
    }

    @Test
    @Order(3)
    void batchJobTest() throws Exception {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("inactiveAccountIds", List.of(1L, 2L, 3L, 4L, 5L).toString());
        paramsBuilder.addDate("date", new Date());
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(paramsBuilder.toJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();

        assertEquals("inactiveAccountIdsJob", actualJobInstance.getJobName());
        assertEquals(ExitStatus.COMPLETED.getExitCode(), actualJobExitStatus.getExitCode());
        assertEquals(1, stepExecutions.size());

        StepExecution stepExecution = stepExecutions.stream().findFirst().get();
        assertEquals(5, stepExecution.getWriteCount());
        assertEquals(3, stepExecution.getCommitCount()); // Batches of 2, eg (1, 2), (3, 4), (5)

        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    @Order(4)
    void batchJobWithRetrySuccessTest() throws Exception {
        reset(processor);
        when(processor.process(anyLong()))
                .thenThrow(new RuntimeException()) // 1st Attempt -> Fail
                .thenThrow(new RuntimeException()) // 2nd Attempt -> Fail
                .thenCallRealMethod();             // 3rd Attempt -> Success

        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("inactiveAccountIds", List.of(1L, 2L, 3L, 4L, 5L).toString());
        paramsBuilder.addDate("date", new Date());
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(paramsBuilder.toJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();

        assertEquals("inactiveAccountIdsJob", actualJobInstance.getJobName());
        assertEquals(ExitStatus.COMPLETED.getExitCode(), actualJobExitStatus.getExitCode()); // JOB succeeded eventually
        assertEquals(1, stepExecutions.size());

        StepExecution stepExecution = stepExecutions.stream().findFirst().get();
        assertEquals(5, stepExecution.getWriteCount());
        assertEquals(3, stepExecution.getCommitCount()); // Batches of 2, eg (1, 2), (3, 4), (5)

        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    @Order(5)
    void batchJobWithRetryFailureTest() throws Exception {
        reset(processor);
        when(processor.process(anyLong()))
                .thenThrow(new RuntimeException())  // 1st Attempt -> Fail
                .thenThrow(new RuntimeException())  // 2nd Attempt -> Fail
                .thenThrow(new RuntimeException()); // 3rd Attempt -> Fail

        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("inactiveAccountIds", List.of(1L, 2L, 3L, 4L, 5L).toString());
        paramsBuilder.addDate("date", new Date());
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(paramsBuilder.toJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();

        assertEquals("inactiveAccountIdsJob", actualJobInstance.getJobName());
        assertEquals(ExitStatus.FAILED.getExitCode(), actualJobExitStatus.getExitCode()); // JOB Failed
        assertEquals(1, stepExecutions.size());

        StepExecution stepExecution = stepExecutions.stream().findFirst().get();
        assertEquals(0, stepExecution.getWriteCount());
        assertEquals(0, stepExecution.getCommitCount());

        jobRepositoryTestUtils.removeJobExecutions();
    }
}
