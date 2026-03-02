package com.mountrich.krushimitra.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.AddAddressActivity;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.models.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    private Context context;
    private List<Address> list;
    private String userId;
    private FirebaseFirestore db;


    public AddressAdapter(Context context, List<Address> list, String userId) {
        this.context = context;
        this.list = list;
        this.userId = userId;
        db = FirebaseFirestore.getInstance();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_address, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        Address a = list.get(pos);

        String fullAddress =
                a.getName() + "\n" +
                        a.getPhone() + "\n" +
                        a.getAddressLine() + ", " +
                        a.getCity() + ", " +
                        a.getState() + " - " +
                        a.getPincode();

        h.txtAddress.setText(fullAddress);
        h.radioDefault.setChecked(a.isDefault());

        h.radioDefault.setOnClickListener(v -> setDefaultAddress(a));

        h.itemView.setOnClickListener(v -> {

            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedAddress", fullAddress);

            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity.setResult(Activity.RESULT_OK, resultIntent);
                activity.finish();
            }
        });

        // EDIT
        h.btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(context, AddAddressActivity.class);
            intent.putExtra("addressId", a.getId());
            intent.putExtra("name", a.getName());
            intent.putExtra("phone", a.getPhone());
            intent.putExtra("addressLine", a.getAddressLine());
            intent.putExtra("city", a.getCity());
            intent.putExtra("state", a.getState());
            intent.putExtra("pincode", a.getPincode());

            context.startActivity(intent);
        });

// DELETE
        h.btnDelete.setOnClickListener(v -> {

            db.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(a.getId())
                    .delete()
                    .addOnSuccessListener(unused -> {

                        list.remove(pos);
                        notifyItemRemoved(pos);

                        Toast.makeText(context,
                                "Address Deleted",
                                Toast.LENGTH_SHORT).show();
                    });
        });

        if (a.isDefault()) {
            h.txtDefaultBadge.setVisibility(View.VISIBLE);
            h.radioDefault.setChecked(true);
        } else {
            h.txtDefaultBadge.setVisibility(View.GONE);
            h.radioDefault.setChecked(false);
        }
    }

    private void setDefaultAddress(Address selected) {

        for (Address a : list) {

            boolean isSelected = a.getId().equals(selected.getId());

            db.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(a.getId())
                    .update("isDefault", isSelected);

            a.setDefault(isSelected);  // Update local list
        }

        notifyDataSetChanged();
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtAddress,txtDefaultBadge;
        RadioButton radioDefault;
        ImageView btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            txtAddress = v.findViewById(R.id.txtAddress);
            radioDefault = v.findViewById(R.id.radioDefault);
            txtDefaultBadge = v.findViewById(R.id.txtDefaultBadge);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}