package com.example.batchprocessing.batch;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.batch.item.ItemWriter;
import com.example.batchprocessing.service.InactiveAccountsService;

@Component
public class InactiveAccountIdsWriter implements ItemWriter<Long> {

    private final InactiveAccountsService inactiveAccountsService;

    public InactiveAccountIdsWriter(InactiveAccountsService inactiveAccountsService) {
        this.inactiveAccountsService = inactiveAccountsService;
    }

    @Override
    public void write(List<? extends Long> items) throws Exception {
        inactiveAccountsService.createEntries(List.copyOf(items));
    }
}
