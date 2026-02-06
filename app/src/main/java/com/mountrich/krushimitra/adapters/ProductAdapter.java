package com.mountrich.krushimitra.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.models.Product;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    Context context;
    List<Product> list;
    FirebaseFirestore db;
    FirebaseAuth auth;

    public ProductAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Product p = list.get(position);

        h.name.setText(p.getName());
        h.price.setText("₹ " + p.getPrice());
        Picasso.get().load(p.getImageUrl()).into(h.image);

        h.addCart.setOnClickListener(v -> addToCart(p));
    }

    private void addToCart(Product p) {
        String uid = auth.getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("name", p.getName());
        map.put("price", p.getPrice());
        map.put("imageUrl", p.getImageUrl());
        map.put("quantity", 1);

        db.collection("cart")
                .document(uid)
                .collection("items")
                .document(p.getId())
                .set(map)
                .addOnSuccessListener(a ->
                        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price;
        Button addCart;

        ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.imgProduct);
            name = v.findViewById(R.id.txtName);
            price = v.findViewById(R.id.txtPrice);
            addCart = v.findViewById(R.id.btnAddCart);
        }
    }
}
