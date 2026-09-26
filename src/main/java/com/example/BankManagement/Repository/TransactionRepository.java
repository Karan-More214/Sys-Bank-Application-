package com.example.BankManagement.Repository;

import com.example.BankManagement.Entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findTop5ByEmailOrderByTimestampDesc(String email);
    List<TransactionEntity> findByEmailOrderByTimestampDesc(String email);
}
