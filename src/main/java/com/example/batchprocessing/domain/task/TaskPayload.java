package com.example.batchprocessing.domain.task;

import java.io.Serializable;
import com.example.batchprocessing.domain.task.inactive.accounts.InactiveAccountsTaskPayload;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        property = "className"
)
@JsonSubTypes({
        @JsonSubTypes.Type(InactiveAccountsTaskPayload.class)
})
public interface TaskPayload extends Serializable {
}
