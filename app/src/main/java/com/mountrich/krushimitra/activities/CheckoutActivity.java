package com.mountrich.krushimitra.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.adapters.CartAdapter;
import com.mountrich.krushimitra.models.CartItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    RecyclerView rvCheckout;
    TextView txtTotalAmount, txtSelctedAddress;
    MaterialButton btnConfirmOrder;

    FirebaseFirestore db;
    FirebaseAuth auth;

    List<CartItem> list = new ArrayList<>();
    CartAdapter adapter;
    int totalAmount = 0;

    private ActivityResultLauncher<Intent> addressLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadCartItems();
        loadDefaultAddress();

        btnConfirmOrder.setOnClickListener(v -> placeOrder());

        addressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("selectedAddress");
                        txtSelctedAddress.setText(address);
                    }
                });
    }

    private void initViews() {
        rvCheckout = findViewById(R.id.rvCheckout);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        txtSelctedAddress = findViewById(R.id.txtSelectedAddress);

        rvCheckout.setLayoutManager(new LinearLayoutManager(this));

        String userId = auth.getUid();
        if (userId == null) finish();

        adapter = new CartAdapter(this, list, userId, this::updateTotal);
        rvCheckout.setAdapter(adapter);

        // Select address click
        txtSelctedAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectAddressActivity.class);
            addressLauncher.launch(intent);
        });
    }

    private void loadCartItems() {
        String userId = auth.getUid();
        if (userId == null) return;

        db.collection("cart").document(userId).collection("items").get()
                .addOnSuccessListener(query -> {
                    list.clear();
                    for (DocumentSnapshot d : query) {
                        CartItem item = d.toObject(CartItem.class);
                        if (item != null) {
                            item.setProductId(d.getId());
                            list.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateTotal();
                });
    }

    private void updateTotal() {
        totalAmount = 0;
        for (CartItem item : list) {
            totalAmount += item.getPrice() * item.getQuantity();
        }
        txtTotalAmount.setText("Total: ₹ " + totalAmount);
    }

    private void loadDefaultAddress() {
        String userId = auth.getUid();
        if (userId == null) return;

        db.collection("users").document(userId).collection("addresses")
                .whereEqualTo("isDefault", true).limit(1).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        String address = doc.getString("name") + "\n" +
                                doc.getString("phone") + "\n" +
                                doc.getString("addressLine") + ", " +
                                doc.getString("city") + ", " +
                                doc.getString("state") + " - " +
                                doc.getString("pincode");
                        txtSelctedAddress.setText(address);
                    } else {
                        txtSelctedAddress.setText("Select Delivery Address");
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultAddress();
    }

    private void placeOrder() {

        String userId = auth.getUid();
        if (userId == null) return;

        // ✅ Validation
        if (list.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (txtSelctedAddress.getText().toString().contains("Select")) {
            Toast.makeText(this, "Please select delivery address", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Generate Order ID
        String orderId = db.collection("orders").document().getId();

        // ✅ Create Order Map
        java.util.Map<String, Object> order = new java.util.HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", userId);
        order.put("totalAmount", totalAmount);
        order.put("status", "Placed");
        order.put("paymentMethod", "COD");
        order.put("paymentStatus", "Pending");
        order.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        order.put("deliveryAddress", txtSelctedAddress.getText().toString());

        // ✅ Add Items List
        java.util.List<java.util.Map<String, Object>> orderItems = new java.util.ArrayList<>();

        for (CartItem item : list) {

            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("productId", item.getProductId());
            map.put("name", item.getName());
            map.put("price", item.getPrice());
            map.put("quantity", item.getQuantity());
            map.put("imageUrl", item.getImageUrl());

            orderItems.add(map);
        }

        order.put("items", orderItems);

        // ✅ Save Order to Firestore
        db.collection("orders")
                .document(orderId)
                .set(order)
                .addOnSuccessListener(unused -> {

                    // ✅ Clear Cart after order
                    clearCart(userId);

                    Toast.makeText(this, "Order Placed Successfully ✅", Toast.LENGTH_LONG).show();

                    finish(); // go back
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Order Failed ❌", Toast.LENGTH_SHORT).show());
    }

    private void clearCart(String userId) {

        db.collection("cart")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(query -> {

                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    for (DocumentSnapshot d : query) {
                        batch.delete(d.getReference());
                    }

                    batch.commit();
                });
    }
}