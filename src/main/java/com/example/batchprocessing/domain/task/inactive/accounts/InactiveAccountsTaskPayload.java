package com.example.batchprocessing.domain.task.inactive.accounts;

import com.example.batchprocessing.domain.task.TaskPayload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class InactiveAccountsTaskPayload implements TaskPayload {
    private Long accountId;
}
