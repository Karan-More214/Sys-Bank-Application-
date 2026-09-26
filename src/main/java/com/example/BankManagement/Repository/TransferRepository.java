package com.example.BankManagement.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BankManagement.Entity.TransferEntity;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, Long> {

    List<TransferEntity> findByFromEmailOrToEmailOrderByCreatedAtDesc(String fromEmail, String toEmail);

    List<TransferEntity> findAllByOrderByCreatedAtDesc();

    List<TransferEntity> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);
}
