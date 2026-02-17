package com.mountrich.krushimitra;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText edtUsername, edtPassword;
    MaterialButton btnLogin, btnGoogle;
    ProgressBar progressBar;
    TextView tvRegister;

    FirebaseAuth auth;
    FirebaseFirestore db;
    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        configureGoogleSignIn();

        btnLogin.setOnClickListener(v -> {
            if (validateLogin()) {
                loginWithEmail();
            }
        });

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edtUsernameLogin);
        edtPassword = findViewById(R.id.edtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.google_sign_up_btn);
        progressBar = findViewById(R.id.loginProgressbar);
        tvRegister = findViewById(R.id.tv_new_user);
    }

    // ---------------- EMAIL LOGIN ----------------
    private void loginWithEmail() {

        setInProgress(true);

        String email = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    setInProgress(false);

                    if (task.isSuccessful()) {
                        goToHome();
                    } else {
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ---------------- GOOGLE CONFIG ----------------
    private void configureGoogleSignIn() {

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                        try {
                            GoogleSignInAccount account =
                                    task.getResult(ApiException.class);

                            firebaseAuthWithGoogle(account.getIdToken());

                        } catch (ApiException e) {
                            Toast.makeText(this,
                                    "Google Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

    private void signInWithGoogle() {
        googleLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();

                        Map<String, Object> map = new HashMap<>();
                        map.put("name", user.getDisplayName());
                        map.put("email", user.getEmail());
                        map.put("mobile", "");
                        map.put("username", "");

                        db.collection("users")
                                .document(user.getUid())
                                .set(map);

                        goToHome();

                    } else {
                        Toast.makeText(this,
                                "Authentication Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------- COMMON ----------------
    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void setInProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : GONE);
        btnLogin.setEnabled(!show);
        btnGoogle.setEnabled(!show);
    }

    // ---------------- VALIDATION ----------------
    private boolean validateLogin() {

        String email = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (email.isEmpty()) {
            edtUsername.setError("Required");
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Min 6 characters");
            return false;
        }

        return true;
    }
}
