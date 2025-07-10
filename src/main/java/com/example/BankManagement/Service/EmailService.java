package com.example.BankManagement.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender javaMailSender;
	@Async
	public void sendEmail(String toEmail,String subject,String message)
	{
	SimpleMailMessage mailMessage=new SimpleMailMessage();
	mailMessage.setTo(toEmail);
	mailMessage.setSubject(subject);
	mailMessage.setText(message);
	mailMessage.setFrom("morekaran3131@gmail.com");
	javaMailSender.send(mailMessage);

	}
}
