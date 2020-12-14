package com.example.batchprocessing.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.batchprocessing.domain.account.Account;
import com.example.batchprocessing.domain.operation.Operation;
import com.example.batchprocessing.domain.operation.OperationPayload;
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

@Service
public class InactiveAccountsServiceImpl implements InactiveAccountsService {

    private final AccountRepository accountRepository;
    private final TaskRepository taskRepository;
    private final OperationRepository operationRepository;

    public InactiveAccountsServiceImpl(AccountRepository accountRepository,
                                       TaskRepository taskRepository,
                                       OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.taskRepository = taskRepository;
        this.operationRepository = operationRepository;
    }

    @Override
    @Transactional
    public void createEntries(List<Long> inactiveAccountIds) {
        List<Account> inactiveAccounts = accountRepository.findAllById(inactiveAccountIds);
        Map<Account, Operation> operations = inactiveAccounts.parallelStream()
                .map(account -> {
                    OperationPayload payload = new InactiveAccountsOperationPayload(account.getId(), account.getEmail());
                    Operation operation = new Operation(payload, OperationType.TERMINATE_ACCOUNT, OperationStatus.PENDING);
                    return Map.entry(account, operation);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        operationRepository.saveAll(operations.values());

        List<Task> tasks = operations.entrySet().parallelStream()
                .map(entry -> new Task(new InactiveAccountsTaskPayload(entry.getKey().getId()),
                                       TaskType.TERMINATE_ACCOUNT,
                                       TaskStatus.PENDING,
                                       TaskResult.NOT_STARTED,
                                       entry.getValue()))
                .collect(Collectors.toList());
        taskRepository.saveAll(tasks);
    }
}
