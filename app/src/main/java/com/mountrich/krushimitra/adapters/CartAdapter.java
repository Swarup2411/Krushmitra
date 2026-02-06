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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.Common.CartItem;
import com.mountrich.krushimitra.R;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> list;
    private FirebaseFirestore db;
    private String userId;

    public CartAdapter(Context context, List<CartItem> list, String userId) {
        this.context = context;
        this.list = list;
        this.userId = userId;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
            .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        CartItem item = list.get(pos);

        h.txtName.setText(item.getName());
        h.txtPrice.setText("₹ " + item.getPrice());
        h.txtQty.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
            .load(item.getImageUrl())
            .into(h.imgProduct);

        h.btnPlus.setOnClickListener(v ->
        updateQty(item, item.getQuantity() + 1));

        h.btnMinus.setOnClickListener(v -> {
        if (item.getQuantity() > 1)
            updateQty(item, item.getQuantity() - 1);
        else
            removeItem(item);
    });
    }

    private void updateQty(CartItem item, int qty) {

        if (item.getProductId() == null || userId == null) {
            Toast.makeText(context, "Invalid cart item", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("cart")
                .document(userId)
                .collection("items")
                .document(item.getProductId())
                .update("quantity", qty);
    }


    private void removeItem(CartItem item) {
        db.collection("cart")
            .document(userId)
            .collection("items")
            .document(item.getProductId())
            .delete();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice, txtQty;
        Button btnPlus, btnMinus;

        ViewHolder(View v) {
            super(v);
            imgProduct = v.findViewById(R.id.imgProduct);
            txtName = v.findViewById(R.id.txtName);
            txtPrice = v.findViewById(R.id.txtPrice);
            txtQty = v.findViewById(R.id.txtQty);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
        }
    }
}
