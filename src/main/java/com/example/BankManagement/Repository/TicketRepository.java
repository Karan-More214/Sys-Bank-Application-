package com.example.BankManagement.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BankManagement.Entity.TicketEntity;
import com.example.BankManagement.Entity.UserEntity;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    List<TicketEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    List<TicketEntity> findAllByOrderByCreatedAtDesc();

    List<TicketEntity> findByStatusOrderByCreatedAtDesc(String status);

    List<TicketEntity> findByPriorityOrderByCreatedAtDesc(String priority);

    List<TicketEntity> findByStatusAndPriorityOrderByCreatedAtDesc(String status, String priority);

    long countByStatusIn(List<String> statuses);
}
