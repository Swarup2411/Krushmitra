package com.mountrich.krushimitra.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import com.google.firebase.firestore.ListenerRegistration;


public class CartFragment extends Fragment {

    RecyclerView rvCart;
    TextView txtTotal;
    FirebaseFirestore db;
    FirebaseAuth auth;
    List<CartItem> list = new ArrayList<>();
    CartAdapter adapter;

    Button btnPlaceOrder;

    boolean emptyToastShown = false;
    ListenerRegistration cartListener;


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

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setAlpha(0.5f); // visually disabled

        btnPlaceOrder.setOnClickListener(vi -> showPaymentDialog());



        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (cartListener != null) {
            cartListener.remove();
        }
    }

    private void loadCart() {

        cartListener = db.collection("cart")
                .document(auth.getUid())
                .collection("items")
                .addSnapshotListener((value, error) -> {

                    if (!isAdded() || getContext() == null) return;

                    if (value == null || value.isEmpty()) {
                        list.clear();
                        adapter.notifyDataSetChanged();

                        txtTotal.setText("Total: ₹ 0");
                        btnPlaceOrder.setEnabled(false);
                        btnPlaceOrder.setAlpha(0.5f);

                        if (!emptyToastShown) {
                            Toast.makeText(getContext(),
                                    "Cart is empty",
                                    Toast.LENGTH_SHORT).show();
                            emptyToastShown = true;
                        }
                        return;
                    }

                    emptyToastShown = false;
                    list.clear();
                    int total = 0;

                    for (DocumentSnapshot d : value) {

                        CartItem item = d.toObject(CartItem.class);
                        if (item == null || item.getQuantity() <= 0) continue;

                        item.setProductId(d.getId());
                        list.add(item);
                        total += item.getPrice() * item.getQuantity();
                    }

                    txtTotal.setText(" ₹ " + total);
                    adapter.notifyDataSetChanged();

                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setAlpha(1f);
                });
    }



    private void showPaymentDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Select Payment Method")
                .setMessage("Choose how you want to pay")
                .setPositiveButton("Cash on Delivery", (d, w) -> {
                    placeOrderCOD();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void placeOrderCOD() {

        String userId = auth.getUid();
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

                        Long priceL = d.getLong("price");
                        Long qtyL = d.getLong("quantity");

                        if (priceL == null || qtyL == null || qtyL <= 0) continue;

                        totalAmount += priceL.intValue() * qtyL.intValue();
                    }

                    String orderId = db.collection("orders").document().getId();

                    Map<String, Object> order = new HashMap<>();
                    order.put("userId", userId);
                    order.put("orderId", orderId);
                    order.put("totalAmount", totalAmount);
                    order.put("paymentMethod", "COD");
                    order.put("paymentStatus", "Pending");
                    order.put("status", "Placed");
                    order.put("timestamp", System.currentTimeMillis());
                    order.put("items", orderItems);   // ✅ FIXED

                    db.collection("orders")
                            .document(orderId)
                            .set(order)
                            .addOnSuccessListener(aVoid -> {

                                clearCart(userId);

                                Toast.makeText(getContext(),
                                        "Order placed successfully",
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

