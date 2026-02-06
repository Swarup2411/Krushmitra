package com.mountrich.krushimitra.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.Common.CartItem;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.adapters.CartAdapter;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    RecyclerView rvCart;
    TextView txtTotal;
    FirebaseFirestore db;
    FirebaseAuth auth;
    List<CartItem> list = new ArrayList<>();
    CartAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCart = v.findViewById(R.id.rvCart);
        txtTotal = v.findViewById(R.id.txtTotal);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(getContext(), list, auth.getUid());
        rvCart.setAdapter(adapter);

        loadCart();

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

                        // 🔥 VERY IMPORTANT
                        item.setProductId(d.getId());

                        list.add(item);
                        total += item.getPrice() * item.getQuantity();
                    }

                    txtTotal.setText("Total: ₹ " + total);
                    adapter.notifyDataSetChanged();
                });
    }

}
