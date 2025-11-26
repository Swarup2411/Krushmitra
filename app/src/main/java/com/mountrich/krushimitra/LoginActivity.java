package com.mountrich.krushimitra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsernameLogin, edtPasswordLogin;
    TextView tvNewUser;
    Button btnLogin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Login User");
        }
        setContentView(R.layout.activity_login);
        edtUsernameLogin = findViewById(R.id.edtUsernameLogin);
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvNewUser = findViewById(R.id.tv_new_user);

        tvNewUser.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            validateLogin();
        });
    }

    private void validateLogin() {

        String username = edtUsernameLogin.getText().toString().trim();
        String password = edtPasswordLogin.getText().toString().trim();

        // ------------------------------
        // 1️⃣ Check if username empty
        // ------------------------------
        if (username.isEmpty()) {
            edtUsernameLogin.setError("Please enter username / mobile / email");
            edtUsernameLogin.requestFocus();
            return;
        }

        // ------------------------------
        // 2️⃣ Check if email valid
        // ------------------------------
        if (username.contains("@")) {  // user entered email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                edtUsernameLogin.setError("Invalid email address");
                edtUsernameLogin.requestFocus();
                return;
            }
        }

        // ------------------------------
        // 3️⃣ Check if mobile valid
        // ------------------------------
        if (username.matches("\\d+")) { // only digits
            if (username.length() != 10) {
                edtUsernameLogin.setError("Mobile number must be 10 digits");
                edtUsernameLogin.requestFocus();
                return;
            }
        }

        // ------------------------------
        // 4️⃣ Username case (text)
        // ------------------------------
        if (!username.contains("@") && !username.matches("\\d+")) {
            if (username.length() < 3) {
                edtUsernameLogin.setError("Username must be at least 3 characters");
                edtUsernameLogin.requestFocus();
                return;
            }
        }

        // ------------------------------
        // 5️⃣ Password validation
        // ------------------------------
        if (password.isEmpty()) {
            edtPasswordLogin.setError("Please enter password");
            edtPasswordLogin.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPasswordLogin.setError("Password must be at least 6 characters");
            edtPasswordLogin.requestFocus();
            return;
        }

        // ------------------------------
        // 6️⃣ If all validations pass
        // ------------------------------
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

        // TODO: Add your login logic or navigate to Dashboard
    }

}
