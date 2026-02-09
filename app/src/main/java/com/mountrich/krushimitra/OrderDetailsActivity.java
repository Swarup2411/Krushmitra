package com.mountrich.krushimitra;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.Common.CartItem;
import com.mountrich.krushimitra.adapters.OrderItemsAdapter;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailsActivity extends AppCompatActivity {

    RecyclerView rv;
    TextView txtOrderId, txtOrderStatus, txtPaymentMethod,
            txtPaymentStatus, txtTotalAmount;

    FirebaseFirestore db;
    List<CartItem> list = new ArrayList<>();
    OrderItemsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        rv = findViewById(R.id.rvOrderItems);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        txtPaymentStatus = findViewById(R.id.txtPaymentStatus);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemsAdapter(this, list);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        String orderId = getIntent().getStringExtra("orderId");

        txtOrderId.setText("Order ID: " + orderId);

        loadOrderDetails(orderId);
        loadOrderItems(orderId);
    }

    private void loadOrderItems(String orderId) {
        db.collection("orders")
                .document(orderId)
                .collection("items")
                .get()
                .addOnSuccessListener(query -> {

                    list.clear();
                    for (DocumentSnapshot d : query) {
                        CartItem item = d.toObject(CartItem.class);
                        list.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void loadOrderDetails(String orderId) {

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        long total = doc.getLong("totalAmount");
                        String orderStatus = doc.getString("status");
                        String paymentStatus = doc.getString("paymentStatus");
                        String paymentMethod = doc.getString("paymentMethod");

                        txtOrderStatus.setText("Order Status : " + orderStatus);
                        txtPaymentMethod.setText("Payment : " + paymentMethod);
                        txtPaymentStatus.setText("Payment Status : " + paymentStatus);
                        txtTotalAmount.setText("Total Amount : ₹ " + total);

                        // Color logic
                        if ("Pending".equals(paymentStatus)) {
                            txtPaymentStatus.setTextColor(Color.parseColor("#D84315")); // Orange
                        } else {
                            txtPaymentStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
                        }
                    }
                });
    }
}
