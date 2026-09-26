package com.example.BankManagement.Service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BankManagement.Entity.TicketEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.TicketRepository;

@Service
@Transactional
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    public TicketEntity createTicket(TicketEntity ticket) {
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketEntity> getTicketsByUser(UserEntity user) {
        return ticketRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public TicketEntity getTicketById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TicketEntity> getTickets(String status, String priority) {
        if (status != null && priority != null) {
            return ticketRepository.findByStatusAndPriorityOrderByCreatedAtDesc(status, priority);
        }
        if (status != null) {
            return ticketRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        if (priority != null) {
            return ticketRepository.findByPriorityOrderByCreatedAtDesc(priority);
        }
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    public TicketEntity respond(Long id, String status, String adminResponse, UserEntity admin) {
        TicketEntity ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            return null;
        }
        ticket.setStatus(status);
        if (adminResponse != null && !adminResponse.trim().isEmpty()) {
            ticket.setAdminResponse(adminResponse.trim());
        }
        ticket.setResolvedBy(admin);
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public long countOpenTickets() {
        return ticketRepository.countByStatusIn(Arrays.asList("OPEN", "IN_PROGRESS"));
    }
}
