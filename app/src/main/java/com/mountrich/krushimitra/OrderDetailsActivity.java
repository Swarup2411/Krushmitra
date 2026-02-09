package com.mountrich.krushimitra;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    TextView txtOrderId, txtStatus,txtTotalAmount;

    FirebaseFirestore db;
    List<CartItem> list = new ArrayList<>();
    OrderItemsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        rv = findViewById(R.id.rvOrderItems);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtStatus = findViewById(R.id.txtStatus);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderItemsAdapter(this, list);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        String orderId = getIntent().getStringExtra("orderId");
        String status = getIntent().getStringExtra("status");

        txtOrderId.setText("Order ID: " + orderId);
        txtStatus.setText("Status: " + status);

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
                        double total = doc.getDouble("totalAmount");
                        txtTotalAmount.setText("Total: ₹ " + total);
                    }
                });
    }

}
