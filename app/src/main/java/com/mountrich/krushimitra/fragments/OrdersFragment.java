package com.mountrich.krushimitra.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.adapters.OrdersAdapter;
import com.mountrich.krushimitra.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerOrders;
    private TextView txtEmpty;

    private OrdersAdapter adapter;
    private List<Order> orderList;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_orders, container, false);

        recyclerOrders = view.findViewById(R.id.recyclerOrders);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        orderList = new ArrayList<>();
        adapter = new OrdersAdapter(getContext(), orderList);
        recyclerOrders.setAdapter(adapter);

        loadOrders();

        return view;
    }

    private void loadOrders() {

        String userId = auth.getUid();
        if (userId == null) return;

        db.collection("orders")
                .whereEqualTo("userId", userId)
// ////               .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {

                    orderList.clear();

                    for (DocumentSnapshot doc : snapshots) {
                        try {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                order.setOrderId(doc.getId());
                                orderList.add(order);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        recyclerOrders.setVisibility(View.GONE);
                        txtEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerOrders.setVisibility(View.VISIBLE);
                        txtEmpty.setVisibility(View.GONE);
                    }
                });
    }

}
