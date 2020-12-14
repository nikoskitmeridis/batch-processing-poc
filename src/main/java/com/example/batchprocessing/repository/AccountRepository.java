package com.example.batchprocessing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.batchprocessing.domain.account.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
