package com.mountrich.krushimitra.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.adapters.AddressAdapter;
import com.mountrich.krushimitra.models.Address;

import java.util.ArrayList;
import java.util.List;

public class SelectAddressActivity extends AppCompatActivity {

    RecyclerView rvAddresses;
    MaterialButton btnAddNewAddress;

    FirebaseFirestore db;
    FirebaseAuth auth;

    List<Address> list = new ArrayList<>();
    AddressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        rvAddresses = findViewById(R.id.recyclerAddress);
        btnAddNewAddress = findViewById(R.id.btnAddNewAddress);

        rvAddresses.setLayoutManager(new LinearLayoutManager(this));

        String userId = auth.getUid();
        if (userId == null) {
            finish();
            return;
        }

        adapter = new AddressAdapter(this, list, userId);
        rvAddresses.setAdapter(adapter);

        btnAddNewAddress.setOnClickListener(v -> {
            startActivity(new Intent(this, AddAddressActivity.class));
        });

        loadAddresses();


    }

    private void loadAddresses() {

        String userId = auth.getUid();
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("addresses")
                .get()
                .addOnSuccessListener(query -> {

                    list.clear();

                    for (DocumentSnapshot d : query) {

                        Address address = d.toObject(Address.class);
                        if (address == null) continue;

                        address.setId(d.getId());
                        list.add(address);
                    }

                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load addresses",
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();  // Refresh when coming back from AddAddressActivity
    }
}