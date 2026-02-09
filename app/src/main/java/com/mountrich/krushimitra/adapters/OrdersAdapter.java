package com.mountrich.krushimitra.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mountrich.krushimitra.OrderDetailsActivity;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.models.Order;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.Holder> {

    Context context;
    List<Order> list;

    public OrdersAdapter(Context c, List<Order> l) {
        context = c;
        list = l;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order, p, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        Order o = list.get(pos);

        h.txtId.setText("Order ID: " + o.getOrderId());
        h.txtAmount.setText("₹ " + o.getTotalAmount());
        h.txtStatus.setText(o.getStatus());

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, OrderDetailsActivity.class);
            i.putExtra("orderId", o.getOrderId());
            i.putExtra("status", o.getStatus());
            context.startActivity(i);
        });

        h.txtPayment.setText(o.getPaymentMethod());


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView txtId, txtAmount, txtStatus,txtPayment;

        Holder(View v) {
            super(v);
            txtId = v.findViewById(R.id.txtOrderId);
            txtAmount = v.findViewById(R.id.txtOrderAmount);
            txtStatus = v.findViewById(R.id.txtOrderStatus);
            txtPayment = v.findViewById(R.id.txtPayment);
        }
    }
}
