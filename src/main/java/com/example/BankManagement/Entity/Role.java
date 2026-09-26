package com.example.BankManagement.Entity;

/**
 * UserEntity.role stays a plain String column (34 call sites across the codebase already
 * compare it case-insensitively as "Admin"/"User"); this enum is the single source of truth
 * for deciding that value at registration, not a new persistence type. Role.ADMIN.name()
 * and Role.USER.name() ("ADMIN"/"USER") remain compatible with every existing
 * equalsIgnoreCase(...) check.
 */
public enum Role {
    ADMIN,
    USER;

    // Set once at startup by AdminRoleEnforcer from the admin.designated-email property
    // (backed by the ADMIN_EMAIL env var). This fallback only applies if that never runs.
    private static String designatedAdminEmail = "morekaran3131@gmail.com";

    public static void setDesignatedAdminEmail(String email) {
        designatedAdminEmail = email;
    }

    public static String getDesignatedAdminEmail() {
        return designatedAdminEmail;
    }

    public static Role forEmail(String email) {
        return email != null && designatedAdminEmail.equalsIgnoreCase(email.trim()) ? ADMIN : USER;
    }
}
