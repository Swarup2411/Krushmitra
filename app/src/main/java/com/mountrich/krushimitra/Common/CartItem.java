package com.mountrich.krushimitra.Common;
public class CartItem {

    private String productId;
    private String name;
    private int price;
    private String imageUrl;
    private int quantity;

    public CartItem() {}

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) { // 🔥 REQUIRED
        this.productId = productId;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}

