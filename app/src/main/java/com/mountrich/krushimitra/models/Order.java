package com.mountrich.krushimitra.models;

public class Order {

    private String orderId;
    private int totalAmount;
    private String status;
    private long timestamp;

    public Order() {}

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() { return orderId; }
    public int getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
}
