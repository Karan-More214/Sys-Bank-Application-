package com.example.BankManagement.Config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.LoanEntity;
import com.example.BankManagement.Entity.TicketEntity;
import com.example.BankManagement.Entity.TransactionEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.BankRepo;
import com.example.BankManagement.Repository.LoanRepo;
import com.example.BankManagement.Repository.TicketRepository;
import com.example.BankManagement.Repository.TransactionRepository;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Security.PasswordService;

import net.datafaker.Faker;

/**
 * Seeds ~500 realistic demo users (with bank accounts, transaction history, loan
 * applications and a handful of support tickets) so the app looks like a real
 * banking product rather than a 1-user test dataset.
 *
 * Safety: only runs when userRepo.count() is below SEED_THRESHOLD. Real signups push
 * the count past that threshold permanently, so this never re-seeds and never wipes
 * anything - it only ever adds rows, and only on a near-empty database.
 *
 * To disable entirely, delete this class or remove its @Component annotation.
 *
 * All seeded users share one password (SEED_PASSWORD) so they're actually usable for
 * a demo login. This is fine for a public showcase dataset, but is not how real user
 * passwords should ever be handled - do not seed a database with real user emails.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final int SEED_THRESHOLD = 10;
    private static final int USER_COUNT = 500;
    static final String SEED_PASSWORD = "Seed@1234";

    private static final String[] EMAIL_DOMAINS = { "gmail.com", "yahoo.com", "outlook.com", "hotmail.com" };
    private static final String[] LOAN_TYPES = { "Personal", "Home", "Auto", "Education", "Business" };
    private static final String[] TICKET_CATEGORIES = { "Account", "Loan", "Transaction", "Card", "Other" };
    private static final String[] TICKET_PRIORITIES = { "Low", "Medium", "High" };

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BankRepo bankRepo;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LoanRepo loanRepo;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordService passwordService;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        long existing = userRepo.count();
        if (existing >= SEED_THRESHOLD) {
            System.out.println("=== DataSeeder: " + existing + " users already present (>= " + SEED_THRESHOLD + "), skipping seed ===");
            return;
        }

        long startedAt = System.currentTimeMillis();
        System.out.println("=== DataSeeder: seeding " + USER_COUNT + " demo users (found only " + existing + ") ===");

        Faker faker = new Faker(Locale.ENGLISH);

        UserEntity admin = userRepo.findAll().stream()
                .filter(u -> "Admin".equalsIgnoreCase(u.getRole()))
                .findFirst()
                .orElse(null);

        List<UserEntity> users = new ArrayList<>(USER_COUNT);
        List<String> phoneNumbers = new ArrayList<>(USER_COUNT);
        List<LocalDate> dobs = new ArrayList<>(USER_COUNT);

        for (int i = 0; i < USER_COUNT; i++) {
            String firstName = sanitizeName(faker.name().firstName());
            String lastName = sanitizeName(faker.name().lastName());
            if (firstName.isEmpty()) firstName = "Alex";
            if (lastName.isEmpty()) lastName = "Doe";

            String emailLocal = (firstName + "." + lastName).toLowerCase(Locale.ENGLISH).replaceAll("[^a-z.]", "");
            String email = emailLocal + i + "@" + pick(EMAIL_DOMAINS);
            String username = (firstName + lastName + i).replaceAll("[^a-zA-Z0-9_]", "");
            if (username.length() > 20) username = username.substring(0, 20);

            UserEntity user = new UserEntity();
            user.setFullName(firstName + " " + lastName);
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(passwordService.encode(SEED_PASSWORD));
            user.setRole("User");
            users.add(user);

            phoneNumbers.add(randomIndianPhone());
            dobs.add(randomAdultDob());
        }

        users = userRepo.saveAll(users);
        System.out.println("=== DataSeeder: saved " + users.size() + " users ===");

        List<BankEntity> accounts = new ArrayList<>(USER_COUNT);
        List<TransactionEntity> allTransactions = new ArrayList<>();
        List<LoanEntity> loans = new ArrayList<>();
        List<TicketEntity> tickets = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            UserEntity user = users.get(i);
            String[] nameParts = user.getFullName().split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            String phone = phoneNumbers.get(i);
            LocalDate dob = dobs.get(i);
            String gender = random.nextBoolean() ? "Male" : "Female";
            String address = faker.address().fullAddress();

            BankEntity account = new BankEntity();
            account.setFirstname(firstName);
            account.setLastname(lastName);
            account.setEmail(user.getEmail());
            account.setPhoneNo(phone);
            account.setGender(gender);
            account.setDob(dob.toString());
            account.setAddress(address);
            account.setAccountType(random.nextInt(4) == 0 ? "current" : "savings");
            account.setAccountStatus(random.nextInt(10) == 0 ? "INACTIVE" : "ACTIVE");
            double balance = randomBalance();
            account.setBalance(balance);
            accounts.add(account);

            int txnCount = 3 + random.nextInt(8);
            for (int t = 0; t < txnCount; t++) {
                TransactionEntity txn = new TransactionEntity();
                txn.setEmail(user.getEmail());
                txn.setType(random.nextInt(100) < 55 ? "Deposit" : "Withdraw");
                double amount = round2(100 + random.nextDouble() * Math.max(1000, balance * 0.2));
                txn.setAmount(amount);
                txn.setTimestamp(randomPastDateTime(365));
                allTransactions.add(txn);
            }

            if (random.nextInt(100) < 18) {
                loans.add(buildLoan(user, firstName, lastName, phone, dob, address));
            }

            if (random.nextInt(100) < 5) {
                tickets.add(buildTicket(faker, user, admin));
            }
        }

        bankRepo.saveAll(accounts);
        System.out.println("=== DataSeeder: saved " + accounts.size() + " bank accounts ===");

        transactionRepository.saveAll(allTransactions);
        System.out.println("=== DataSeeder: saved " + allTransactions.size() + " transactions ===");

        loanRepo.saveAll(loans);
        System.out.println("=== DataSeeder: saved " + loans.size() + " loan applications ===");

        ticketRepository.saveAll(tickets);
        System.out.println("=== DataSeeder: saved " + tickets.size() + " support tickets ===");

        long elapsedMs = System.currentTimeMillis() - startedAt;
        System.out.println("=== DataSeeder: done in " + elapsedMs + "ms. Demo login password for all seeded users: " + SEED_PASSWORD + " ===");
    }

    private LoanEntity buildLoan(UserEntity user, String firstName, String lastName, String phone, LocalDate dob, String address) {
        LoanEntity loan = new LoanEntity();
        loan.setUser(user);
        loan.setFirstname(firstName);
        loan.setLastname(lastName);
        loan.setDob(dob);
        loan.setEmail(user.getEmail());
        loan.setPhoneNo(phone);
        loan.setAddress(address);
        String loanType = pick(LOAN_TYPES);
        loan.setLoanType(loanType);
        loan.setLoanAmount(round2(50000 + random.nextDouble() * 1950000));
        loan.setLoanYears(1 + random.nextInt(20));
        loan.setEmploymentType(random.nextBoolean() ? "Salaried" : "Self-Employed");
        loan.setMonthlyIncome(round2(20000 + random.nextDouble() * 180000));
        loan.setPurpose(loanPurpose(loanType));
        loan.setApplicationDate(randomPastDateTime(365));

        int bucket = random.nextInt(100);
        if (bucket < 40) {
            loan.setStatus("PENDING");
            loan.setPaid(false);
        } else if (bucket < 70) {
            loan.setStatus("APPROVED");
            loan.setPaid(false);
        } else if (bucket < 90) {
            loan.setStatus("APPROVED");
            loan.setPaid(true);
        } else {
            loan.setStatus("REJECTED");
            loan.setPaid(false);
        }
        return loan;
    }

    private TicketEntity buildTicket(Faker faker, UserEntity user, UserEntity admin) {
        TicketEntity ticket = new TicketEntity();
        ticket.setUser(user);
        String category = pick(TICKET_CATEGORIES);
        ticket.setCategory(category);
        ticket.setPriority(pick(TICKET_PRIORITIES));
        ticket.setSubject(ticketSubject(category));
        ticket.setDescription(faker.lorem().sentence(15));
        ticket.setCreatedAt(randomPastDateTime(60));

        int bucket = random.nextInt(100);
        if (bucket < 40) {
            ticket.setStatus("OPEN");
        } else if (bucket < 65) {
            ticket.setStatus("IN_PROGRESS");
        } else if (bucket < 90) {
            ticket.setStatus("RESOLVED");
            ticket.setAdminResponse("Thanks for reaching out - this has been resolved on our end.");
            ticket.setResolvedBy(admin);
        } else {
            ticket.setStatus("CLOSED");
            ticket.setAdminResponse("Closing this ticket as no further action is needed.");
            ticket.setResolvedBy(admin);
        }
        return ticket;
    }

    private String loanPurpose(String loanType) {
        return switch (loanType) {
            case "Home" -> "Purchase of a residential property";
            case "Auto" -> "Purchase of a new vehicle";
            case "Education" -> "Tuition and education expenses";
            case "Business" -> "Working capital for a small business";
            default -> "Personal expenses";
        };
    }

    private String ticketSubject(String category) {
        return switch (category) {
            case "Loan" -> "Question about my loan application";
            case "Transaction" -> "Transaction not reflecting in history";
            case "Card" -> "Issue with card access";
            case "Other" -> "General inquiry";
            default -> "Question about my account";
        };
    }

    private String sanitizeName(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[^a-zA-Z\\s]", "").trim();
    }

    private String pick(String[] options) {
        return options[random.nextInt(options.length)];
    }

    private String randomIndianPhone() {
        return "9" + String.format("%09d", random.nextInt(1_000_000_000));
    }

    private LocalDate randomAdultDob() {
        int age = 21 + random.nextInt(45);
        int year = LocalDate.now().getYear() - age;
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        return LocalDate.of(year, month, day);
    }

    private LocalDateTime randomPastDateTime(int maxDaysAgo) {
        int daysAgo = 1 + random.nextInt(maxDaysAgo);
        return LocalDateTime.now()
                .minusDays(daysAgo)
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60));
    }

    private double randomBalance() {
        int band = random.nextInt(100);
        double balance;
        if (band < 40) {
            balance = 500 + random.nextDouble() * 24500;
        } else if (band < 75) {
            balance = 25000 + random.nextDouble() * 225000;
        } else if (band < 95) {
            balance = 250000 + random.nextDouble() * 1250000;
        } else {
            balance = 1500000 + random.nextDouble() * 3500000;
        }
        return round2(balance);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
