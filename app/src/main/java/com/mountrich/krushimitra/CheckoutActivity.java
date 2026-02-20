package com.mountrich.krushimitra;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.mountrich.krushimitra.Common.CartItem;
import com.mountrich.krushimitra.adapters.CartAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    RecyclerView rvCheckout;
    TextView txtTotalAmount;
    Button btnConfirmOrder;

    FirebaseFirestore db;
    FirebaseAuth auth;

    List<CartItem> list = new ArrayList<>();
    CartAdapter adapter;

    int totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        Toast.makeText(this, "Checkout opened", Toast.LENGTH_SHORT).show();

        initViews();
        loadCartItems();

        btnConfirmOrder.setOnClickListener(v -> placeOrder());


    }

    private void initViews() {
        rvCheckout = findViewById(R.id.rvCheckout);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        rvCheckout.setLayoutManager(new LinearLayoutManager(this));
        String userId = auth.getUid();
        if (userId == null) {
            finish();
            return;
        }

        adapter = new CartAdapter(this, list, userId);

        rvCheckout.setAdapter(adapter);
    }

    private void loadCartItems() {

        String userId = auth.getUid();
        if (userId == null) return;

        db.collection("cart")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(query -> {

                    list.clear();
                    totalAmount = 0;

                    for (DocumentSnapshot d : query) {

                        CartItem item = d.toObject(CartItem.class);
                        if (item == null) continue;

                        item.setProductId(d.getId());
                        list.add(item);

                        totalAmount += item.getPrice() * item.getQuantity();
                    }

                    txtTotalAmount.setText("Total: ₹ " + totalAmount);
                    adapter.notifyDataSetChanged();
                });
    }

    private void placeOrder() {

        String userId = auth.getUid();
        if (userId == null) return;

        if (list.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderId = db.collection("orders").document().getId();

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", userId);
        order.put("totalAmount", totalAmount);
        order.put("status", "Placed");
        order.put("paymentMethod", "COD");
        order.put("paymentStatus", "Pending");
        order.put("timestamp", FieldValue.serverTimestamp());

        List<Map<String, Object>> orderItems = new ArrayList<>();

        for (CartItem item : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", item.getProductId());
            map.put("name", item.getName());
            map.put("price", item.getPrice());
            map.put("quantity", item.getQuantity());
            map.put("imageUrl", item.getImageUrl());
            orderItems.add(map);
        }

        order.put("items", orderItems);

        db.collection("orders")
                .document(orderId)
                .set(order)
                .addOnSuccessListener(unused -> {

                    clearCart(userId);

                    Toast.makeText(this,
                            "Order Placed Successfully",
                            Toast.LENGTH_LONG).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Order Failed",
                                Toast.LENGTH_SHORT).show());
    }

    private void clearCart(String userId) {

        db.collection("cart")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(query -> {

                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot d : query) {
                        batch.delete(d.getReference());
                    }

                    batch.commit();
                });
    }
}
