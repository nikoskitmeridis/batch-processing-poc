package com.example.batchprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.batchprocessing.domain.task.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
