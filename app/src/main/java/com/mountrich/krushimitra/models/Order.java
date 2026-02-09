package com.mountrich.krushimitra.models;

public class Order {

    private String orderId;
    private String userId;

    private int totalAmount;

    private String paymentMethod;   // COD
    private String paymentStatus;   // Pending / Paid

    private String status;           // Placed / Shipped / Delivered
    private long timestamp;

    // 🔹 Required empty constructor for Firestore
    public Order() {}

    // 🔹 Getters & Setters

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
