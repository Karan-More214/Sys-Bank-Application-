package com.example.BankManagement.Repository;

    import java.util.List;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import com.example.BankManagement.Entity.LoanEntity;
    import com.example.BankManagement.Entity.UserEntity;

    @Repository
    public interface LoanRepo extends JpaRepository<LoanEntity, Integer> {
        
        /**
         * Find all loans for a specific user
         */
        List<LoanEntity> findByUser(UserEntity user);
        
        /**
         * Find loans by email
         */
        List<LoanEntity> findByEmail(String email);
        
        /**
         * Find loans by loan type
         */
        List<LoanEntity> findByLoanType(String loanType);
        
        /**
         * Find loans by user email (alternative method)
         */
        List<LoanEntity> findByUserEmail(String email);
        
        /**
         * Find loans by user ID
         */
        List<LoanEntity> findByUserId(int userId);
        
        /**
         * Find loans by loan status
         */
        List<LoanEntity> findByLoanStatus(LoanEntity.LoanStatus loanStatus);
        
        /**
         * Find loans by payment status
         */
        List<LoanEntity> findByPaymentStatus(LoanEntity.PaymentStatus paymentStatus);
        
        /**
         * Find loans by loan status and payment status
         */
        List<LoanEntity> findByLoanStatusAndPaymentStatus(LoanEntity.LoanStatus loanStatus, LoanEntity.PaymentStatus paymentStatus);
        
        /**
         * Find loans by user and loan status
         */
        List<LoanEntity> findByUserAndLoanStatus(UserEntity user, LoanEntity.LoanStatus loanStatus);
    }
