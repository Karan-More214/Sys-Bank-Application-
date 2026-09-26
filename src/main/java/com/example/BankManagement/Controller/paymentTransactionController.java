package com.example.BankManagement.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.BankManagement.Entity.orderTransactionDetails;
import com.example.BankManagement.Service.orderServices;

@RestController
public class paymentTransactionController 
{
	@Autowired
    private orderServices orderservices;
    
    @CrossOrigin(origins = "*")
    @GetMapping("/getTransaction/{amount}")
    public orderTransactionDetails getTransaction(@PathVariable(name="amount") double amount) {
        System.out.println("🔵 Creating Razorpay order for: ₹" + amount);
        
        orderTransactionDetails transactionDetails = orderservices.orderCreateTransaction(amount);
        
        if (transactionDetails != null) {
            System.out.println("✅ Order created - Amount in paise: " + transactionDetails.getAmount());
            System.out.println("✅ Order ID: " + transactionDetails.getOrderId());
            return transactionDetails;
        } else {
            System.err.println("❌ Failed to create order");
            return null;
        }
    }
		
	}

	
	
