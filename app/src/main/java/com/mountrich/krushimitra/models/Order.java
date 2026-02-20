package com.mountrich.krushimitra.models;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class Order {

    private String orderId;
    private String userId;
    private int totalAmount;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private Timestamp timestamp;
    private List<Map<String, Object>> items;

    // 🔥 REQUIRED empty constructor
    public Order() {
    }

    // Getters & Setters

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }
}
