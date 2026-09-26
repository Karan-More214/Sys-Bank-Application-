package com.example.BankManagement.Controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BankManagement.Entity.TicketEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Service.TicketService;
import com.example.BankManagement.dto.TicketCreateRequest;
import com.example.BankManagement.dto.TicketDTO;
import com.example.BankManagement.dto.TicketRespondRequest;

import jakarta.validation.Valid;

/**
 * Support ticket REST API. User endpoints raise/view own tickets; admin endpoints
 * list/filter all tickets and respond to them. Identity comes from the validated JWT
 * (Authentication), never a client-supplied email/adminEmail.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketApiController {

    private static final List<String> VALID_STATUSES = List.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");
    private static final List<String> VALID_CATEGORIES = List.of("Account", "Loan", "Transaction", "Card", "Other");
    private static final List<String> VALID_PRIORITIES = List.of("Low", "Medium", "High");

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> raiseTicket(@Valid @RequestBody TicketCreateRequest request, BindingResult bindingResult,
                                          Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        if (!VALID_CATEGORIES.contains(request.getCategory())) {
            return error(HttpStatus.BAD_REQUEST, "Invalid category");
        }
        if (!VALID_PRIORITIES.contains(request.getPriority())) {
            return error(HttpStatus.BAD_REQUEST, "Invalid priority");
        }

        try {
            UserEntity user = userRepo.findByEmail(authentication.getName());
            if (user == null) {
                return error(HttpStatus.NOT_FOUND, "User not found");
            }

            TicketEntity ticket = new TicketEntity();
            ticket.setUser(user);
            ticket.setSubject(request.getSubject().trim());
            ticket.setDescription(request.getDescription().trim());
            ticket.setCategory(request.getCategory());
            ticket.setPriority(request.getPriority());

            TicketEntity saved = ticketService.createTicket(ticket);
            return ResponseEntity.status(HttpStatus.CREATED).body(TicketDTO.fromEntity(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while raising the ticket.");
        }
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> myTickets(Authentication authentication) {
        try {
            UserEntity user = userRepo.findByEmail(authentication.getName());
            if (user == null) {
                return error(HttpStatus.NOT_FOUND, "User not found");
            }

            List<TicketDTO> tickets = ticketService.getTicketsByUser(user).stream()
                    .map(TicketDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while loading your tickets.");
        }
    }

    /** Either the ticket's own owner or an admin may view it - can't express "owner OR admin" via @PreAuthorize role alone, so it's checked in the body. */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicket(@PathVariable("id") Long id, Authentication authentication) {
        try {
            TicketEntity ticket = ticketService.getTicketById(id);
            if (ticket == null) {
                return error(HttpStatus.NOT_FOUND, "Ticket not found");
            }

            UserEntity requester = userRepo.findByEmail(authentication.getName());
            boolean isOwner = requester != null && ticket.getUser() != null
                    && ticket.getUser().getEmail().equalsIgnoreCase(requester.getEmail());
            boolean isAdmin = requester != null && "Admin".equalsIgnoreCase(requester.getRole());

            if (!isOwner && !isAdmin) {
                return error(HttpStatus.FORBIDDEN, "You do not have access to this ticket");
            }

            return ResponseEntity.ok(TicketDTO.fromEntity(ticket));
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while loading the ticket.");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listTickets(@RequestParam(value = "status", required = false) String status,
                                          @RequestParam(value = "priority", required = false) String priority) {
        if (status != null && !VALID_STATUSES.contains(status)) {
            return error(HttpStatus.BAD_REQUEST, "Invalid status filter");
        }
        if (priority != null && !VALID_PRIORITIES.contains(priority)) {
            return error(HttpStatus.BAD_REQUEST, "Invalid priority filter");
        }

        List<TicketDTO> tickets = ticketService.getTickets(status, priority).stream()
                .map(TicketDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> respond(@PathVariable("id") Long id,
                                      @Valid @RequestBody TicketRespondRequest request,
                                      BindingResult bindingResult,
                                      Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        if (!VALID_STATUSES.contains(request.getStatus())) {
            return error(HttpStatus.BAD_REQUEST, "Invalid status");
        }

        UserEntity admin = userRepo.findByEmail(authentication.getName());
        if (admin == null) {
            return error(HttpStatus.NOT_FOUND, "Admin user not found");
        }

        TicketEntity updated = ticketService.respond(id, request.getStatus(), request.getAdminResponse(), admin);
        if (updated == null) {
            return error(HttpStatus.NOT_FOUND, "Ticket not found");
        }

        return ResponseEntity.ok(TicketDTO.fromEntity(updated));
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, String> fieldErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : bindingResult.getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return errors;
    }
}
