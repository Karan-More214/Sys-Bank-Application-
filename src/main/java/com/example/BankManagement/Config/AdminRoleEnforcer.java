package com.example.BankManagement.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.BankManagement.Entity.Role;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.UserRepo;

/**
 * Registration already assigns Role.forEmail(...) correctly for new signups, but that
 * logic can't retroactively fix a row created before it existed (e.g. from earlier
 * testing, seeded with role=User). Runs on every boot, independent of DataSeeder's
 * early-return-when-already-seeded check, so it also fixes an already-seeded production
 * database on its next deploy without needing manual SQL.
 */
@Component
public class AdminRoleEnforcer implements CommandLineRunner {

    @Autowired
    private UserRepo userRepo;

    @Value("${admin.designated-email}")
    private String designatedAdminEmail;

    @Override
    public void run(String... args) {
        Role.setDesignatedAdminEmail(designatedAdminEmail);

        UserEntity designatedAdmin = userRepo.findByEmail(designatedAdminEmail);
        if (designatedAdmin != null && !Role.ADMIN.name().equalsIgnoreCase(designatedAdmin.getRole())) {
            designatedAdmin.setRole(Role.ADMIN.name());
            userRepo.save(designatedAdmin);
            System.out.println("=== AdminRoleEnforcer: promoted " + designatedAdminEmail + " to ADMIN ===");
        }
    }
}
