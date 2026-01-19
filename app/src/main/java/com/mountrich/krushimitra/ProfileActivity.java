package com.mountrich.krushimitra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    // UI
    private CircleImageView imgProfile;
    private EditText edtName, edtEmail, edtMobile;
    private Button btnUpdate, btnLogout;
    private ProgressBar profileProgressBar;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private String userId;
    private Uri imageUri;

    private ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
        }

        initFirebase();
        initViews();
        initImagePicker();
        setupListeners();

        loadUserData();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = auth.getCurrentUser().getUid();
    }

    private void initViews() {
        imgProfile = findViewById(R.id.imgProfile);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtMobile = findViewById(R.id.edtMobile);
        btnUpdate = findViewById(R.id.btnUpdateProfile);
        btnLogout = findViewById(R.id.btnLogout);
        profileProgressBar = findViewById(R.id.profileProgressBar);
    }

    private void initImagePicker() {
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgProfile.setImageURI(uri);
                        uploadProfileImage();
                    }
                }
        );
    }

    private void setupListeners() {
        imgProfile.setOnClickListener(v ->
                imagePicker.launch("image/*")
        );

        btnUpdate.setOnClickListener(v -> updateProfile());

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // ---------------- IMAGE UPLOAD ----------------
    private void uploadProfileImage() {

        if (imageUri == null) return;

        profileProgressBar.setVisibility(View.VISIBLE);

        StorageReference imageRef =
                storageRef.child("profile_images/" + userId + "_" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(task ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                            db.collection("users")
                                    .document(userId)
                                    .update("profileImage", uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        profileProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(this,
                                                "Profile photo updated",
                                                Toast.LENGTH_SHORT).show();
                                    });
                        })
                )
                .addOnFailureListener(e -> {
                    profileProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Upload failed",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ---------------- LOAD DATA ----------------
    private void loadUserData() {

        profileProgressBar.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        edtName.setText(doc.getString("name"));
                        edtEmail.setText(doc.getString("email"));
                        edtMobile.setText(doc.getString("mobile"));

                        String imageUrl = doc.getString("profileImage");
                        if (imageUrl != null) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.krushi_img)
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(
                                            com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                    .into(imgProfile);
                        }
                    }

                    profileProgressBar.setVisibility(View.GONE);
                });
    }

    // ---------------- UPDATE PROFILE ----------------
    private void updateProfile() {

        String name = edtName.getText().toString().trim();
        String mobile = edtMobile.getText().toString().trim();

        if (name.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this,
                    "All fields required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("mobile", mobile);

        db.collection("users")
                .document(userId)
                .update(map)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this,
                                "Profile Updated",
                                Toast.LENGTH_SHORT).show());
    }
}
