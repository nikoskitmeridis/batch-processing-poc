package com.example.batchprocessing.service;

import java.util.List;

public interface InactiveAccountsService {
    void createEntries(List<Long> inactiveAccountIds);
}
