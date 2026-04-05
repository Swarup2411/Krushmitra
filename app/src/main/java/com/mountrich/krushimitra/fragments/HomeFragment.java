package com.mountrich.krushimitra.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
    ProductAdapter productAdapter;
    SwipeRefreshLayout swipeRefresh;
    ShimmerFrameLayout shimmerLayout;
    Spinner spinner;
    TextView txtNoProducts;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        txtNoProducts = view.findViewById(R.id.txtNoProducts);

        String[] categories = {
                "All",
                "Seeds",
                "Fertilizer",
                "Irrigation",
                "Tools",
                "Pesticides"
        };

        spinner = view.findViewById(R.id.categorySpinner);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        categories);

        spinner.setAdapter(adapter);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(getContext(), productList,this);
        recyclerView.setAdapter(productAdapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedCategory = categories[position];

                if(selectedCategory.equals("All")){
                    loadAllProducts();
                }
                else{
                    loadProducts(selectedCategory);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Pull To Refresh
        swipeRefresh.setOnRefreshListener(() -> {
            spinner.setSelection(0);
            loadAllProducts();
        });

        return view;
    }

//    private void loadProducts() {
//
//
//        db.collection("products")
//                .get()
//                .addOnSuccessListener(query -> {
//
//                    productList.clear();
//
//                    for (DocumentSnapshot d : query) {
//
//                        Product p = d.toObject(Product.class);
//
//                        if (p != null) {
//                            p = new Product(
//                                    d.getId(),
//                                    p.getName(),
//                                    p.getPrice(),
//                                    p.getImageUrl(),
//                                    p.getDescription()
//                            );
//
//                            productList.add(p);
//                        }
//                    }
//
//                    adapter.notifyDataSetChanged();
//
//                    shimmerLayout.stopShimmer();
//                    shimmerLayout.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);
//
//                    swipeRefresh.setRefreshing(false);
//                })
//                .addOnFailureListener(e -> {
//                    shimmerLayout.stopShimmer();
//                    shimmerLayout.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);
//                    swipeRefresh.setRefreshing(false);
//                });
//    }

    private void loadProducts(String category){

        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);

        db.collection("products")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    productList.clear();

                    for(DocumentSnapshot doc : queryDocumentSnapshots){

                        Product product = doc.toObject(Product.class);


                        if(product != null){
                            product = new Product(
                                    doc.getId(),
                                    product.getName(),
                                    product.getPrice(),
                                    product.getImageUrl(),
                                    product.getDescription()
                            );
                        }

                        productList.add(product);
                    }

                    productAdapter.notifyDataSetChanged();

                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);

                    if(productList.isEmpty()){
                        txtNoProducts.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    else{
                        txtNoProducts.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    swipeRefresh.setRefreshing(false);
                }) .addOnFailureListener(e -> {
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void loadAllProducts(){

        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);


        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    productList.clear();

                    for(DocumentSnapshot doc : queryDocumentSnapshots){

                        Product product = doc.toObject(Product.class);

                        if(product != null){
                            product = new Product(
                                    doc.getId(),
                                    product.getName(),
                                    product.getPrice(),
                                    product.getImageUrl(),
                                    product.getDescription()
                            );
                        }

                        productList.add(product);
                    }

                    productAdapter.notifyDataSetChanged();

                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);

                    if(productList.isEmpty()){
                        txtNoProducts.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    else{
                        txtNoProducts.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    swipeRefresh.setRefreshing(false);
                }) .addOnFailureListener(e -> {
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(false);
                });
    }
}
