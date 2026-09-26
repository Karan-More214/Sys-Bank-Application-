package com.example.BankManagement.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BankManagement.Entity.NoticeEntity;
import com.example.BankManagement.Repository.NoticeRepository;

@Service
@Transactional
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    public NoticeEntity create(NoticeEntity notice) {
        return noticeRepository.save(notice);
    }

    @Transactional(readOnly = true)
    public List<NoticeEntity> getActive() {
        return noticeRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<NoticeEntity> getAll() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public NoticeEntity getById(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }

    public NoticeEntity update(NoticeEntity notice) {
        return noticeRepository.save(notice);
    }

    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }
}
