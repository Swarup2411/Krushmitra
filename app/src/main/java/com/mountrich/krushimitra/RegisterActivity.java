package com.mountrich.krushimitra;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtNameRegister, edtMobileRegister, edtEmailRegister,
            edtUsernameRegister, edtPasswordRegister;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView alreadyAcc;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClicks();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        progressBar = findViewById(R.id.registerProgressbar);
        edtNameRegister = findViewById(R.id.edtNameRegister);
        edtMobileRegister = findViewById(R.id.edtMobileRegister);
        edtEmailRegister = findViewById(R.id.edtEmailRegister);
        edtUsernameRegister = findViewById(R.id.edtUsernameRegister);
        edtPasswordRegister = findViewById(R.id.edtPasswordRegister);
        btnRegister = findViewById(R.id.btnRegister);
        alreadyAcc = findViewById(R.id.alreadyAcc);
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
        progressBar.setVisibility(inProgress ? View.VISIBLE : GONE);
        btnRegister.setVisibility(inProgress ? GONE : View.VISIBLE);
    }

    private void registerUser() {
        setInProgress(true);

        String email = edtEmailRegister.getText().toString().trim();
        String password = edtPasswordRegister.getText().toString().trim();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String userId = auth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", edtNameRegister.getText().toString().trim());
                        user.put("mobile", edtMobileRegister.getText().toString().trim());
                        user.put("email", email);
                        user.put("username", edtUsernameRegister.getText().toString().trim());

                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    setInProgress(false);
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    setInProgress(false);
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        setInProgress(false);
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs() {
        String name = edtNameRegister.getText().toString().trim();
        String email = edtEmailRegister.getText().toString().trim();
        String mobile = edtMobileRegister.getText().toString().trim();
        String username = edtUsernameRegister.getText().toString().trim();
        String password = edtPasswordRegister.getText().toString().trim();

        if (name.length() < 3) {
            edtNameRegister.setError("Enter valid name");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmailRegister.setError("Enter valid email");
            return false;
        }

        if (mobile.length() != 10 || !mobile.matches("[0-9]+")) {
            edtMobileRegister.setError("Enter valid 10-digit mobile");
            return false;
        }

        if (username.isEmpty()) {
            edtUsernameRegister.setError("Username required");
            return false;
        }

        if (password.length() < 6) {
            edtPasswordRegister.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }
}
