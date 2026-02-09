package com.mountrich.krushimitra.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.Common.CartItem;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.adapters.CartAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartFragment extends Fragment {

    RecyclerView rvCart;
    TextView txtTotal;
    FirebaseFirestore db;
    FirebaseAuth auth;
    List<CartItem> list = new ArrayList<>();
    CartAdapter adapter;

    Button btnPlaceOrder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCart = v.findViewById(R.id.rvCart);
        txtTotal = v.findViewById(R.id.txtTotal);

        btnPlaceOrder = v.findViewById(R.id.btnPlaceOrder);

                auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(getContext(), list, auth.getUid());
        rvCart.setAdapter(adapter);


        loadCart();

        btnPlaceOrder.setOnClickListener(vi -> placeOrder());


        return v;
    }

    private void loadCart() {

        db.collection("cart")
                .document(auth.getUid())
                .collection("items")
                .addSnapshotListener((value, error) -> {

                    list.clear();
                    int total = 0;

                    for (DocumentSnapshot d : value) {

                        CartItem item = d.toObject(CartItem.class);

                        // 🔥 THIS LINE FIXES YOUR CRASH
                        item.setProductId(d.getId());


                        list.add(item);
                        total += item.getPrice() * item.getQuantity();
                    }

                    txtTotal.setText("Total: ₹ " + total);
                    adapter.notifyDataSetChanged();
                });
    }

    private void placeOrder() {
        btnPlaceOrder.setEnabled(false);

        String userId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (userId == null) return;

        db.collection("cart")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int totalAmount = 0;
                    List<Map<String, Object>> orderItems = new ArrayList<>();

                    for (DocumentSnapshot d : query) {
                        Map<String, Object> item = d.getData();
                        orderItems.add(item);

                        int price = d.getLong("price").intValue();
                        int qty = d.getLong("quantity").intValue();
                        totalAmount += price * qty;
                    }

                    // 🔥 Create Order
                    String orderId = db.collection("orders").document().getId();

                    Map<String, Object> order = new HashMap<>();
                    order.put("userId", userId);
                    order.put("totalAmount", totalAmount);
                    order.put("status", "Placed");
                    order.put("timestamp", System.currentTimeMillis());

                    db.collection("orders")
                            .document(orderId)
                            .set(order)
                            .addOnSuccessListener(aVoid -> {

                                // 🔥 Add items to order
                                for (DocumentSnapshot d : query) {
                                    db.collection("orders")
                                            .document(orderId)
                                            .collection("items")
                                            .document(d.getId())
                                            .set(d.getData());
                                }

                                // 🔥 Clear cart
                                clearCart(userId);

                                Toast.makeText(getContext(),
                                        "Order Placed Successfully",
                                        Toast.LENGTH_LONG).show();
                            });
                });
    }

    private void clearCart(String userId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("cart")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot d : query) {
                        d.getReference().delete();
                    }
                });
    }

}

