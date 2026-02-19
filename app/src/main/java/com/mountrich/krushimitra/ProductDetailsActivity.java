package com.mountrich.krushimitra;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.models.Product;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;


public class ProductDetailsActivity extends AppCompatActivity {

    // UI Components
    private ImageView imgProduct, btnFavorite;
    private TextView txtName, txtPrice, txtDescription, txtQuantity;
    private MaterialButton btnAddCart, btnBuyNow, btnPlus, btnMinus;
    private FirebaseAuth auth;
    private Product currentProduct;


    // Firebase
    private FirebaseFirestore db;

    // Variables
    private int quantity = 1;
    private boolean isFavorite = false;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        initViews();
        initFirebase();
        setupQuantityButtons();
        setupFavoriteButton();
        setupButtons();
        loadProductDetails();
    }

    // 🔹 Initialize Views
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



    }

    // 🔹 Initialize Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        productId = getIntent().getStringExtra("productId");
    }

    // 🔹 Quantity Logic
    private void setupQuantityButtons() {

        btnPlus.setOnClickListener(v -> {
            quantity++;
            txtQuantity.setText(String.valueOf(quantity));
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                txtQuantity.setText(String.valueOf(quantity));
            }
        });
    }

    // 🔹 Favorite Toggle Logic
    private void setupFavoriteButton() {

        btnFavorite.setOnClickListener(v -> {

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            isFavorite = !isFavorite;

            if (isFavorite) {

                btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                btnFavorite.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));

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

            } else {

                btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                btnFavorite.setColorFilter(getResources().getColor(R.color.teal_700));

                db.collection("users")
                        .document(userId)
                        .collection("favorites")
                        .document(productId)
                        .delete();
            }
        });
    }


    // 🔹 Cart + Buy Button
    private void setupButtons() {

        btnAddCart.setOnClickListener(v -> {

            if (currentProduct == null) {
                Toast.makeText(this, "Product not loaded yet", Toast.LENGTH_SHORT).show();
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
                    .document(productId);   // ✅ FIXED HERE

            cartRef.get().addOnSuccessListener(snapshot -> {

                if (snapshot.exists()) {
                    // If already in cart → increase quantity
                    Long oldQty = snapshot.getLong("quantity");
                    long newQty = (oldQty != null ? oldQty : 0) + quantity;

                    cartRef.update("quantity", newQty)
                            .addOnSuccessListener(a ->
                                    Toast.makeText(this, "Quantity Updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());

                } else {
                    // If not in cart → create new item
                    cartRef.set(cartItem)
                            .addOnSuccessListener(a ->
                                    Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }

            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });



        btnBuyNow.setOnClickListener(v -> {

            if (currentProduct == null) {
                Toast.makeText(this, "Product not loaded yet", Toast.LENGTH_SHORT).show();
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

    // 🔹 Load Product from Firestore
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
                        }

                    } else {
                        Toast.makeText(this,
                                "Product not available",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error loading product",
                                Toast.LENGTH_SHORT).show());
    }
}
