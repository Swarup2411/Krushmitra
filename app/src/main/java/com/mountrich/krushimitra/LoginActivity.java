package com.mountrich.krushimitra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsernameLogin, edtPasswordLogin;
    TextView tvNewUser;
    Button btnLogin,googleSignUpButton;


    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;
    int RC_SIGN_IN = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);


        edtUsernameLogin = findViewById(R.id.edtUsernameLogin);
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvNewUser = findViewById(R.id.tv_new_user);
        googleSignUpButton = findViewById(R.id.google_sign_up_btn);

        tvNewUser.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            validateLogin();
        });

        googleSignUpButton.setOnClickListener(v->{
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this,
                        "Error code: " + e.getStatusCode(),
                        Toast.LENGTH_LONG).show();
            }

        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        if (firebaseAuth == null) {        firebaseAuth = FirebaseAuth.getInstance();}


        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        Toast.makeText(this,
                                "Welcome " + user.getDisplayName(),
                                Toast.LENGTH_SHORT).show();

                        // Navigate to Home
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
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
