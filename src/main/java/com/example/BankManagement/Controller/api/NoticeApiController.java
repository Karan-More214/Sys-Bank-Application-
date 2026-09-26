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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BankManagement.Entity.NoticeEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Service.NoticeService;
import com.example.BankManagement.dto.NoticeCreateRequest;
import com.example.BankManagement.dto.NoticeDTO;
import com.example.BankManagement.dto.NoticeUpdateRequest;

import jakarta.validation.Valid;

/**
 * Admin-authored notices/announcements. GET /active is public (permitted in
 * SecurityConfig) - anyone can read the current announcement feed. Every write and
 * the full list require a validated JWT with the ADMIN role, checked via
 * @PreAuthorize against the token's role claim, not a client-supplied email.
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeApiController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/active")
    public ResponseEntity<?> activeNotices() {
        List<NoticeDTO> notices = noticeService.getActive().stream()
                .map(NoticeDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notices);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> allNotices() {
        List<NoticeDTO> notices = noticeService.getAll().stream()
                .map(NoticeDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notices);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody NoticeCreateRequest request, BindingResult bindingResult,
                                     Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        UserEntity admin = userRepo.findByEmail(authentication.getName());
        if (admin == null) {
            return error(HttpStatus.NOT_FOUND, "Admin user not found");
        }

        NoticeEntity notice = new NoticeEntity();
        notice.setTitle(request.getTitle().trim());
        notice.setMessage(request.getMessage().trim());
        notice.setCreatedBy(admin);
        notice.setActive(true);

        NoticeEntity saved = noticeService.create(notice);
        return ResponseEntity.status(HttpStatus.CREATED).body(NoticeDTO.fromEntity(saved));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                     @Valid @RequestBody NoticeUpdateRequest request,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        NoticeEntity notice = noticeService.getById(id);
        if (notice == null) {
            return error(HttpStatus.NOT_FOUND, "Notice not found");
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            notice.setTitle(request.getTitle().trim());
        }
        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            notice.setMessage(request.getMessage().trim());
        }
        if (request.getActive() != null) {
            notice.setActive(request.getActive());
        }

        return ResponseEntity.ok(NoticeDTO.fromEntity(noticeService.update(notice)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        NoticeEntity notice = noticeService.getById(id);
        if (notice == null) {
            return error(HttpStatus.NOT_FOUND, "Notice not found");
        }
        noticeService.delete(id);
        return ResponseEntity.noContent().build();
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
