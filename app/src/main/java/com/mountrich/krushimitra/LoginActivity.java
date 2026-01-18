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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnLogin, btnGoogle;
    ProgressBar progressBar;
    TextView tvRegister;

    FirebaseAuth auth;
    FirebaseFirestore db;

    GoogleSignInClient googleSignInClient;
    int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtUsername = findViewById(R.id.edtUsernameLogin);
        edtPassword = findViewById(R.id.edtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.google_sign_up_btn);
        progressBar = findViewById(R.id.loginProgressbar);
        tvRegister = findViewById(R.id.tv_new_user);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> {
            if (validateLogin()) {
                findEmailAndLogin();
            }
        });

        btnGoogle.setOnClickListener(v ->
                startActivityForResult(
                        googleSignInClient.getSignInIntent(),
                        RC_SIGN_IN));

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    // ---------------- FIND EMAIL ----------------
    private void findEmailAndLogin() {
        setInProgress(true);

        String input = edtUsername.getText().toString().trim();

        db.collection("users")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String email = doc.getString("email");
                        String mobile = doc.getString("mobile");
                        String username = doc.getString("username");

                        if (input.equals(email)
                                || input.equals(mobile)
                                || input.equals(username)) {

                            firebaseLogin(email);
                            return;
                        }
                    }

                    setInProgress(false);
                    Toast.makeText(this,
                            "User not found",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ---------------- FIREBASE LOGIN ----------------
    private void firebaseLogin(String email) {

        auth.signInWithEmailAndPassword(
                        email,
                        edtPassword.getText().toString())
                .addOnCompleteListener(task -> {

                    setInProgress(false);

                    if (task.isSuccessful()) {
                        startActivity(new Intent(
                                LoginActivity.this,
                                HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ---------------- GOOGLE RESULT ----------------
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account =
                        task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this,
                        "Google Sign In Failed",
                        Toast.LENGTH_SHORT).show();
            }
        }
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

                        startActivity(new Intent(
                                this,
                                HomeActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this,
                                "Authentication Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------- UI ----------------
    void setInProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : GONE);
        btnLogin.setVisibility(show ? GONE : View.VISIBLE);
    }

    // ---------------- VALIDATION ----------------
    private boolean validateLogin() {

        if (edtUsername.getText().toString().trim().isEmpty()) {
            edtUsername.setError("Required");
            return false;
        }

        if (edtPassword.getText().toString().length() < 6) {
            edtPassword.setError("Min 6 characters");
            return false;
        }

        return true;
    }
}
