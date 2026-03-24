package com.mountrich.krushimitra.adapters;

import android.content.Context;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.models.CartItem;
import com.mountrich.krushimitra.R;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> list;
    private FirebaseFirestore db;
    private String userId;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<CartItem> list, String userId, OnCartChangeListener listener) {
        this.context = context;
        this.list = list;
        this.userId = userId;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        CartItem item = list.get(pos);

        holder.txtName.setText(item.getName());
        holder.txtPrice.setText("₹ " + item.getPrice());
        holder.txtQty.setText(String.valueOf(item.getQuantity()));

        Glide.with(context).load(item.getImageUrl()).into(holder.imgProduct);

        // Increase quantity
        holder.btnPlus.setOnClickListener(v -> updateQty(item, item.getQuantity() + 1, pos));

        // Decrease quantity
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                updateQty(item, item.getQuantity() - 1, pos);
            } else {
                removeItem(item, pos);
            }
        });
    }

    private void updateQty(CartItem item, int qty, int pos) {
        db.collection("cart").document(userId)
                .collection("items").document(item.getProductId())
                .update("quantity", qty)
                .addOnSuccessListener(aVoid -> {
                    item.setQuantity(qty);
                    notifyItemChanged(pos);
                    if (listener != null) listener.onCartUpdated();
                });
    }

    private void removeItem(CartItem item, int pos) {
        db.collection("cart").document(userId)
                .collection("items").document(item.getProductId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    list.remove(pos);
                    notifyItemRemoved(pos);
                    if (listener != null) listener.onCartUpdated();
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice, txtQty;
        MaterialButton btnPlus, btnMinus;

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