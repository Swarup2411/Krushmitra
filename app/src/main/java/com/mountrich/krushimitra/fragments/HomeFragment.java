package com.mountrich.krushimitra.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.adapters.ProductAdapter;
import com.mountrich.krushimitra.models.Product;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    FirebaseFirestore db;
    List<Product> productList;
    ProductAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 1️⃣ Inflate layout FIRST
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 2️⃣ Init views using view.findViewById
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        // 3️⃣ Firebase + list
        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();



        // 4️⃣ Fetch products
        db.collection("products")
                .get()
                .addOnSuccessListener(query -> {
                    productList.clear();
                    for (DocumentSnapshot d : query) {
                        Product p = d.toObject(Product.class);
                        if (p != null) {
                            p = new Product(
                                    d.getId(),
                                    p.getName(),
                                    p.getPrice(),
                                    p.getImageUrl(),
                                    p.getDescription()
                            );
                            productList.add(p);
                        }
                    }

                    adapter = new ProductAdapter(getContext(), productList);
                    recyclerView.setAdapter(adapter);
                });

        return view;
    }
}
