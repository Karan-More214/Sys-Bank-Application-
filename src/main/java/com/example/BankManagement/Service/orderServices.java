package com.example.BankManagement.Service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.BankManagement.Entity.orderTransactionDetails;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Service
public class orderServices {

	@Value("${razorpay.key_id}")
	private String KEY;

	@Value("${razorpay.key_secret}")
	private String KEY_SECRET;

	private static final String CURRENCY="INR";
	
	public orderTransactionDetails orderCreateTransaction(double amount) {
		try {
			System.out.println("🔵 ORIGINAL AMOUNT RECEIVED: ₹" + amount);
			
			JSONObject jsonObject = new JSONObject();
			// ✅ Amount is ALREADY in RUPEES, convert to paise
			int amountInPaise = (int) (amount * 100);
			jsonObject.put("amount", amountInPaise);
			jsonObject.put("currency", CURRENCY);
			
			System.out.println("💰 CONVERTED TO PAISE: " + amountInPaise);
			System.out.println("💰 THIS WILL SHOW AS: ₹" + (amountInPaise / 100.0) + " in Razorpay");
			
			RazorpayClient razorpayClient = new RazorpayClient(KEY, KEY_SECRET);
			Order order = razorpayClient.orders.create(jsonObject);
			
			return orderTransaction(order);
		} catch (Exception e) {
			System.err.println("❌ Error creating Razorpay order: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Verifies a Razorpay checkout callback's signature using the same key/secret
	 * this service used to create the order, without exposing the secret outside it.
	 */
	public boolean verifyPayment(String orderId, String paymentId, String signature) {
		try {
			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", orderId);
			attributes.put("razorpay_payment_id", paymentId);
			attributes.put("razorpay_signature", signature);
			return Utils.verifyPaymentSignature(attributes, KEY_SECRET);
		} catch (Exception e) {
			System.err.println("❌ Error verifying Razorpay payment signature: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private orderTransactionDetails orderTransaction(Order order) {
		String orderid = order.get("id");
		String currency = order.get("currency");
		int amount = order.get("amount");
		
		System.out.println("✅ Order created - ID: " + orderid + ", Amount in paise: " + amount);
		
		orderTransactionDetails orderTransactionDetails = new orderTransactionDetails(
			orderid, 
			currency, 
			amount, 
			KEY
		);
		
		return orderTransactionDetails;
	}

}
