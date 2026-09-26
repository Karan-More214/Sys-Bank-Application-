package com.example.BankManagement.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.BankRepo;
import com.example.BankManagement.Repository.UserRepo;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private BankRepo bankRepo;

    public UserEntity getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }
    
    public BankEntity getBankAccountByEmail(String email) {
        return bankRepo.findByEmail(email);
    }
    
    /**
     * Update user information
     * @param user UserEntity to update
     * @return Updated UserEntity
     */
    public UserEntity updateUser(UserEntity user) {
        return userRepo.save(user);
    }
}