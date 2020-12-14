package com.example.batchprocessing.controller;

import java.util.concurrent.Callable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.batchprocessing.controller.dto.InactiveAccountIdsDto;

@RequestMapping(path = "inactive-accounts",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public interface InactiveAccountsController {

    @PostMapping("create-entries")
    Callable<Void> createEntries(@RequestBody InactiveAccountIdsDto inactiveAccountIdsDto);
}
