package com.mountrich.krushimitra.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mountrich.krushimitra.R;

import java.util.HashMap;
import java.util.Map;

public class AddAddressActivity extends AppCompatActivity {

    TextInputEditText etName, etPhone, etAddress, etCity, etState, etPincode;
    MaterialButton btnSave;

    FirebaseFirestore db;
    FirebaseAuth auth;
    String editId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        etPincode = findViewById(R.id.etPincode);
        btnSave = findViewById(R.id.btnSaveAddress);

        if (getIntent() != null && getIntent().hasExtra("addressId")) {

            editId = getIntent().getStringExtra("addressId");

            etName.setText(getIntent().getStringExtra("name"));
            etPhone.setText(getIntent().getStringExtra("phone"));
            etAddress.setText(getIntent().getStringExtra("addressLine"));
            etCity.setText(getIntent().getStringExtra("city"));
            etState.setText(getIntent().getStringExtra("state"));
            etPincode.setText(getIntent().getStringExtra("pincode"));

            btnSave.setText("Update Address");
        }

        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {

        String userId = auth.getUid();
        if (userId == null) return;

        String name = etName.getText() != null ?
                etName.getText().toString().trim() : "";

        String phone = etPhone.getText() != null ?
                etPhone.getText().toString().trim() : "";

        String address = etAddress.getText() != null ?
                etAddress.getText().toString().trim() : "";

        String city = etCity.getText() != null ?
                etCity.getText().toString().trim() : "";

        String state = etState.getText() != null ?
                etState.getText().toString().trim() : "";

        String pincode = etPincode.getText() != null ?
                etPincode.getText().toString().trim() : "";

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()
                || city.isEmpty() || state.isEmpty() || pincode.isEmpty()) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("phone", phone);
        map.put("addressLine", address);
        map.put("city", city);
        map.put("state", state);
        map.put("pincode", pincode);

        // ✅ EDIT MODE
        if (editId != null) {

            db.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(editId)
                    .update(map)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(this,
                                "Address Updated Successfully",
                                Toast.LENGTH_SHORT).show();

                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Update Failed",
                                    Toast.LENGTH_SHORT).show());

        }
        // ✅ ADD MODE
        else {

            String addressId = db.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document()
                    .getId();

            map.put("id", addressId);
            map.put("isDefault", false);

            db.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(addressId)
                    .set(map)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(this,
                                "Address Saved Successfully",
                                Toast.LENGTH_SHORT).show();

                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Save Failed",
                                    Toast.LENGTH_SHORT).show());
        }
    }
}