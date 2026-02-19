package com.mountrich.krushimitra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtNameRegister, edtMobileRegister, edtEmailRegister,
            edtUsernameRegister, edtPasswordRegister;

    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView alreadyAcc;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClicks();
    }

    private void initViews() {

        edtNameRegister = findViewById(R.id.edtNameRegister);
        edtMobileRegister = findViewById(R.id.edtMobileRegister);
        edtEmailRegister = findViewById(R.id.edtEmailRegister);
        edtUsernameRegister = findViewById(R.id.edtUsernameRegister);
        edtPasswordRegister = findViewById(R.id.edtPasswordRegister);

        btnRegister = findViewById(R.id.btnRegister);
        alreadyAcc = findViewById(R.id.alreadyAcc);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClicks() {

        alreadyAcc.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });
    }

    private void setInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!inProgress);
    }

    private void registerUser() {

        setInProgress(true);

        String name = edtNameRegister.getText().toString().trim();
        String mobile = edtMobileRegister.getText().toString().trim();
        String email = edtEmailRegister.getText().toString().trim();
        String username = edtUsernameRegister.getText().toString().trim();
        String password = edtPasswordRegister.getText().toString().trim();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        setInProgress(false);
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String userId = auth.getCurrentUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("mobile", mobile);
                    user.put("email", email);
                    user.put("username", username);
                    user.put("createdAt", System.currentTimeMillis());

                    db.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener(unused -> {

                                setInProgress(false);

                                Toast.makeText(this,
                                        "Registration Successful",
                                        Toast.LENGTH_SHORT).show();

                                // Go directly to MainActivity (better UX)
                                startActivity(new Intent(this, HomeActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setInProgress(false);
                                Toast.makeText(this,
                                        "Failed to save user data",
                                        Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private boolean validateInputs() {

        if (edtNameRegister.getText().toString().trim().length() < 3) {
            edtNameRegister.setError("Enter valid name");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(
                edtEmailRegister.getText().toString().trim()).matches()) {
            edtEmailRegister.setError("Enter valid email");
            return false;
        }

        if (edtMobileRegister.getText().toString().trim().length() != 10) {
            edtMobileRegister.setError("Enter valid mobile number");
            return false;
        }

        if (edtUsernameRegister.getText().toString().trim().isEmpty()) {
            edtUsernameRegister.setError("Username required");
            return false;
        }

        if (edtPasswordRegister.getText().toString().trim().length() < 6) {
            edtPasswordRegister.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }
}
