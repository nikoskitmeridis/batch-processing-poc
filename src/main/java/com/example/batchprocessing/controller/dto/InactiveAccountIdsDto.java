package com.example.batchprocessing.controller.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InactiveAccountIdsDto {
    private List<Long> inactiveAccountIds;
}
