package com.mountrich.krushimitra.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mountrich.krushimitra.Common.CartItem;
import com.mountrich.krushimitra.OrderDetailsActivity;
import com.mountrich.krushimitra.R;

import java.util.List;


public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.VH> {

    Context context;
    List<CartItem> list;

    public OrderItemsAdapter(Context context, List<CartItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_order_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {

        CartItem item = list.get(pos);

        h.txtName.setText(item.getName());
        h.txtQty.setText("Qty: " + item.getQuantity());
        h.txtPrice.setText("₹ " + item.getPrice());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.krushi_img)
                .into(h.imgProduct);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName, txtQty, txtPrice;

        VH(View v) {
            super(v);
            imgProduct = v.findViewById(R.id.imgProduct);
            txtName = v.findViewById(R.id.txtName);
            txtQty = v.findViewById(R.id.txtQty);
            txtPrice = v.findViewById(R.id.txtPrice);
        }
    }
}
