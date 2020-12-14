package com.example.batchprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.batchprocessing.domain.operation.Operation;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
}
