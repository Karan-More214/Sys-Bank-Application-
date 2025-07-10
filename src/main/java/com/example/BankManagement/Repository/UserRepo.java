package com.example.BankManagement.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BankManagement.Entity.UserEntity;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, Integer> {
	
	 UserEntity findByEmailAndPassword(String email, String password);
	    
	    UserEntity findByEmail(String email);
	    
	    UserEntity findByUsername(String username);
	    
	    boolean existsByEmail(String email);
	    
	    boolean existsByUsername(String username);
	    
	    Optional<UserEntity> findById(int id);
}