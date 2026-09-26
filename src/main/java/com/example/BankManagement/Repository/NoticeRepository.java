package com.example.BankManagement.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BankManagement.Entity.NoticeEntity;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    List<NoticeEntity> findByActiveTrueOrderByCreatedAtDesc();

    List<NoticeEntity> findAllByOrderByCreatedAtDesc();
}
