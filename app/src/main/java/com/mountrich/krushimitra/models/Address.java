package com.mountrich.krushimitra.models;

public class Address {

    private String id;
    private String name;
    private String phone;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private boolean isDefault;

    public Address() {}

    public Address(String name, String phone, String addressLine,
                   String city, String state, String pincode, boolean isDefault) {
        this.name = name;
        this.phone = phone;
        this.addressLine = addressLine;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddressLine() { return addressLine; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }

    public boolean isDefault() { return isDefault; }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}