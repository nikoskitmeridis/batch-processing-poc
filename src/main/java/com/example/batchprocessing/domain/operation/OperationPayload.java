package com.example.batchprocessing.domain.operation;

import java.io.Serializable;
import com.example.batchprocessing.domain.operation.inactive.accounts.InactiveAccountsOperationPayload;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        property = "className"
)
@JsonSubTypes({
        @JsonSubTypes.Type(InactiveAccountsOperationPayload.class)
})
public interface OperationPayload extends Serializable {
}
