package com.example.batchprocessing.domain.operation.inactive.accounts;

import com.example.batchprocessing.domain.operation.OperationPayload;

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
public class InactiveAccountsOperationPayload implements OperationPayload {
    private Long accountId;
    private String email;
}


