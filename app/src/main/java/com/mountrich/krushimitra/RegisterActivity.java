package com.mountrich.krushimitra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    EditText edtNameRegister,edtMobileRegister,edtEmailRegister,edtQualificationRegister,edtAddressRegister,edtUsernameRegister,edtPasswordRegister;
    private Button btnRegister;
    TextView alreadyAcc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_register);

        edtNameRegister = findViewById(R.id.edtNameRegister);
        edtMobileRegister = findViewById(R.id.edtMobileRegister);
        edtEmailRegister = findViewById(R.id.edtEmailRegister);
        edtQualificationRegister = findViewById(R.id.edtQualificationRegister);
        edtAddressRegister = findViewById(R.id.edtAddressRegister);
        edtUsernameRegister = findViewById(R.id.edtUsernameRegister);
        edtPasswordRegister = findViewById(R.id.edtPasswordRegister);
        btnRegister = findViewById(R.id.btnRegister);
        alreadyAcc = findViewById(R.id.alreadyAcc);

        alreadyAcc.setOnClickListener(v->{
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v->{
            if(validateInputs()){
                Toast.makeText(this, "Register Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validateInputs() {
        String name = edtNameRegister.getText().toString().trim();
        String email = edtEmailRegister.getText().toString().trim();
        String mobile = edtMobileRegister.getText().toString().trim();
        String password = edtPasswordRegister.getText().toString().trim();

        // Validate Name
        if (name.isEmpty()) {
            edtNameRegister.setError("Name is required");
            return false;
        }
        if (name.length() < 3) {
            edtNameRegister.setError("Name must be at least 3 characters");
            return false;
        }

        // Validate Email
        if (email.isEmpty()) {
            edtEmailRegister.setError("Email is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmailRegister.setError("Enter valid email");
            return false;
        }

        // Validate Mobile
        if (mobile.isEmpty()) {
            edtMobileRegister.setError("Mobile number required");
            return false;
        }
        if (mobile.length() != 10) {
            edtMobileRegister.setError("Enter 10-digit mobile number");
            return false;
        }
        if (!mobile.matches("[0-9]+")) {
            edtMobileRegister.setError("Mobile number must contain digits only");
            return false;
        }

        // Validate Password
        if (password.isEmpty()) {
            edtPasswordRegister.setError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            edtPasswordRegister.setError("Password must be at least 6 characters");
            return false;
        }

        return true; // All good
    }

}