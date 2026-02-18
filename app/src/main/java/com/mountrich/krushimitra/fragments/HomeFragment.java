package com.mountrich.krushimitra.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
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
    SwipeRefreshLayout swipeRefresh;
    ShimmerFrameLayout shimmerLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();
        adapter = new ProductAdapter(getContext(), productList);
        recyclerView.setAdapter(adapter);

        loadProducts();

        // Pull To Refresh
        swipeRefresh.setOnRefreshListener(() -> {
            loadProducts();
        });

        return view;
    }

    private void loadProducts() {

        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);

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

                    adapter.notifyDataSetChanged();

                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(false);
                });
    }
}
