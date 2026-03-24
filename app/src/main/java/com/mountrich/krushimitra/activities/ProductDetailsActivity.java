package com.mountrich.krushimitra.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.models.Product;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView imgProduct, btnFavorite, btnPlus, btnMinus;
    private TextView txtName, txtPrice, txtDescription, txtQuantity;
    private MaterialButton btnAddCart, btnBuyNow;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Product currentProduct;
    private String productId;
    private int quantity = 1;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        initViews();

        setupQuantityButtons();
        setupFavoriteButton();
        setupButtons();

        loadProductDetails();
    }

    private void initViews() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        productId = getIntent().getStringExtra("productId");

        imgProduct = findViewById(R.id.imgProduct);
        btnFavorite = findViewById(R.id.btnFavorite);
        txtName = findViewById(R.id.txtName);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        txtQuantity = findViewById(R.id.txtQuantity);
        btnAddCart = findViewById(R.id.btnAddCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);

        // Disable Buy Now by default
        btnBuyNow.setEnabled(false);
        btnBuyNow.setAlpha(0.5f);
    }

    // Quantity buttons
    private void setupQuantityButtons() {
        btnPlus.setOnClickListener(v -> {
            if (quantity < 20) {
                quantity++;
                txtQuantity.setText(String.valueOf(quantity));
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                txtQuantity.setText(String.valueOf(quantity));
            }
        });
    }

    // Favorite button
    private void setupFavoriteButton() {
        btnFavorite.setOnClickListener(v -> {

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentProduct == null) return;

            String userId = auth.getCurrentUser().getUid();
            isFavorite = !isFavorite;

            if (isFavorite) {
                btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);

                Map<String, Object> favItem = new HashMap<>();
                favItem.put("productId", productId);
                favItem.put("name", currentProduct.getName());
                favItem.put("price", currentProduct.getPrice());
                favItem.put("imageUrl", currentProduct.getImageUrl());

                db.collection("users")
                        .document(userId)
                        .collection("favorites")
                        .document(productId)
                        .set(favItem);

                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();

            } else {
                btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                db.collection("users")
                        .document(userId)
                        .collection("favorites")
                        .document(productId)
                        .delete();
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add to Cart & Buy Now buttons
    private void setupButtons() {
        btnAddCart.setOnClickListener(v -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Product loading...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();

            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("productId", productId);
            cartItem.put("name", currentProduct.getName());
            cartItem.put("price", currentProduct.getPrice());
            cartItem.put("imageUrl", currentProduct.getImageUrl());
            cartItem.put("quantity", quantity);
            cartItem.put("timestamp", FieldValue.serverTimestamp());

            DocumentReference cartRef = db.collection("cart")
                    .document(userId)
                    .collection("items")
                    .document(productId);

            cartRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    Long oldQty = snapshot.getLong("quantity");
                    long newQty = (oldQty != null ? oldQty : 0) + quantity;
                    cartRef.update("quantity", newQty);
                    Toast.makeText(this, "Cart quantity updated", Toast.LENGTH_SHORT).show();
                } else {
                    cartRef.set(cartItem);
                    Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
                }

                // Enable Buy Now button
                btnBuyNow.setEnabled(true);
                btnBuyNow.setAlpha(1f);
            });

        });

        btnBuyNow.setOnClickListener(v -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Product loading...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("productId", productId);
            intent.putExtra("quantity", quantity);
            startActivity(intent);
        });
    }

    // Load product info
    private void loadProductDetails() {
        if (productId == null) {
            Toast.makeText(this, "Product not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            currentProduct = product;

                            txtName.setText(product.getName());
                            txtPrice.setText("₹" + product.getPrice());
                            txtDescription.setText(product.getDescription());

                            Glide.with(this)
                                    .load(product.getImageUrl())
                                    .into(imgProduct);

                            // ✅ Load saved quantity from cart
                            loadCartQuantity();
                        }
                    } else {
                        Toast.makeText(this, "Product not available", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    // Load existing quantity from cart
    private void loadCartQuantity() {
        if (auth.getCurrentUser() == null || productId == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("cart")
                .document(userId)
                .collection("items")
                .document(productId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Long savedQty = snapshot.getLong("quantity");
                        if (savedQty != null && savedQty > 0) {
                            quantity = savedQty.intValue();
                            txtQuantity.setText(String.valueOf(quantity));
                            btnBuyNow.setEnabled(true);
                            btnBuyNow.setAlpha(1f);
                        }
                    } else {
                        quantity = 1;
                        txtQuantity.setText(String.valueOf(quantity));
                        btnBuyNow.setEnabled(false);
                        btnBuyNow.setAlpha(0.5f);
                    }
                });
    }
}