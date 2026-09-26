package com.example.BankManagement.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.TransactionEntity;
import com.example.BankManagement.Entity.TransferEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.BankRepo;
import com.example.BankManagement.Repository.TransactionRepository;
import com.example.BankManagement.Repository.TransferRepository;

/**
 * Owns the transfer state machine (PENDING/FLAGGED -> COMPLETED -> REVERSED)
 * and every balance movement that goes with it. TransferEntity carries the
 * two-party record + review lifecycle; matching TransactionEntity rows are
 * written whenever money actually moves, so transfers show up in each side's
 * existing transaction history for free.
 */
@Service
@Transactional
public class TransferService {

    /** Transfers at or above this amount are held for admin review instead of moving funds immediately. */
    public static final double FLAG_THRESHOLD = 100000.0;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private BankRepo bankRepo;

    @Autowired
    private TransactionRepository transactionRepository;

    public static class TransferResult {
        public final TransferEntity transfer;
        public final String error;

        private TransferResult(TransferEntity transfer, String error) {
            this.transfer = transfer;
            this.error = error;
        }

        static TransferResult ok(TransferEntity t) { return new TransferResult(t, null); }
        static TransferResult fail(String message) { return new TransferResult(null, message); }
    }

    @Transactional
    public TransferResult initiate(String fromEmail, String toEmail, double amount, String note) {
        if (fromEmail.equalsIgnoreCase(toEmail)) {
            return TransferResult.fail("You cannot transfer money to your own account.");
        }

        BankEntity fromAccount = bankRepo.findByEmail(fromEmail);
        if (fromAccount == null) {
            return TransferResult.fail("Sender bank account not found.");
        }

        BankEntity toAccount = bankRepo.findByEmail(toEmail);
        if (toAccount == null) {
            return TransferResult.fail("No SysBank account found for that recipient.");
        }

        if (amount > fromAccount.getBalance()) {
            return TransferResult.fail("Insufficient balance for this transfer.");
        }

        TransferEntity transfer = new TransferEntity();
        transfer.setFromEmail(fromEmail);
        transfer.setToEmail(toEmail);
        transfer.setAmount(amount);
        transfer.setNote(note);

        if (amount >= FLAG_THRESHOLD) {
            transfer.setStatus("FLAGGED");
            transferRepository.save(transfer);
            return TransferResult.ok(transfer);
        }

        transfer.setStatus("COMPLETED");
        transferRepository.save(transfer);
        moveFunds(fromAccount, toAccount, amount, "Transfer Out", "Transfer In");
        return TransferResult.ok(transfer);
    }

    @Transactional
    public TransferResult approve(Long transferId) {
        TransferEntity transfer = transferRepository.findById(transferId).orElse(null);
        if (transfer == null) {
            return TransferResult.fail("Transfer not found");
        }
        if (!"FLAGGED".equals(transfer.getStatus())) {
            return TransferResult.fail("Only a flagged transfer can be approved (this one is " + transfer.getStatus() + ")");
        }

        BankEntity fromAccount = bankRepo.findByEmail(transfer.getFromEmail());
        BankEntity toAccount = bankRepo.findByEmail(transfer.getToEmail());
        if (fromAccount == null || toAccount == null) {
            return TransferResult.fail("One of the accounts on this transfer no longer exists.");
        }
        if (transfer.getAmount() > fromAccount.getBalance()) {
            return TransferResult.fail("Sender no longer has sufficient balance for this transfer.");
        }

        moveFunds(fromAccount, toAccount, transfer.getAmount(), "Transfer Out", "Transfer In");
        transfer.setStatus("COMPLETED");
        return TransferResult.ok(transferRepository.save(transfer));
    }

    @Transactional
    public TransferResult reverse(Long transferId, String reason) {
        TransferEntity transfer = transferRepository.findById(transferId).orElse(null);
        if (transfer == null) {
            return TransferResult.fail("Transfer not found");
        }
        if (reason == null || reason.trim().isEmpty()) {
            return TransferResult.fail("A reason is required to reverse a transfer.");
        }

        if ("FLAGGED".equals(transfer.getStatus())) {
            // Funds never moved for a flagged transfer - reversing it is just a rejection.
            transfer.setStatus("REVERSED");
            transfer.setReverseReason(reason.trim());
            return TransferResult.ok(transferRepository.save(transfer));
        }

        if ("COMPLETED".equals(transfer.getStatus())) {
            BankEntity fromAccount = bankRepo.findByEmail(transfer.getFromEmail());
            BankEntity toAccount = bankRepo.findByEmail(transfer.getToEmail());
            if (fromAccount == null || toAccount == null) {
                return TransferResult.fail("One of the accounts on this transfer no longer exists.");
            }
            if (transfer.getAmount() > toAccount.getBalance()) {
                return TransferResult.fail("Recipient no longer has sufficient balance to reverse this transfer.");
            }

            // Money moves back: recipient -> sender.
            moveFunds(toAccount, fromAccount, transfer.getAmount(), "Transfer Reversal Debit", "Transfer Reversal Credit");
            transfer.setStatus("REVERSED");
            transfer.setReverseReason(reason.trim());
            return TransferResult.ok(transferRepository.save(transfer));
        }

        return TransferResult.fail("A " + transfer.getStatus() + " transfer cannot be reversed again.");
    }

    public void setReviewer(TransferEntity transfer, UserEntity admin) {
        transfer.setReviewedBy(admin);
        transferRepository.save(transfer);
    }

    private void moveFunds(BankEntity debitAccount, BankEntity creditAccount, double amount, String debitType, String creditType) {
        debitAccount.setBalance(debitAccount.getBalance() - amount);
        creditAccount.setBalance(creditAccount.getBalance() + amount);
        bankRepo.save(debitAccount);
        bankRepo.save(creditAccount);

        TransactionEntity debitTxn = new TransactionEntity();
        debitTxn.setEmail(debitAccount.getEmail());
        debitTxn.setType(debitType);
        debitTxn.setAmount(amount);
        transactionRepository.save(debitTxn);

        TransactionEntity creditTxn = new TransactionEntity();
        creditTxn.setEmail(creditAccount.getEmail());
        creditTxn.setType(creditType);
        creditTxn.setAmount(amount);
        transactionRepository.save(creditTxn);
    }
}
