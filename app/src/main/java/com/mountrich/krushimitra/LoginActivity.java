package com.mountrich.krushimitra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mountrich.krushimitra.Common.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsernameLogin, edtPasswordLogin;
    Button btnLogin, googleSignUpButton;
    TextView tvNewUser;

    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Shared Preferences
        sharedPreferences = getSharedPreferences("KrushiMitra", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Views
        edtUsernameLogin = findViewById(R.id.edtUsernameLogin);
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        googleSignUpButton = findViewById(R.id.google_sign_up_btn);
        tvNewUser = findViewById(R.id.tv_new_user);

        // New User
        tvNewUser.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        // Login Button
        btnLogin.setOnClickListener(v -> {
            if (validateLogin()) {
                loginUser();
            }
        });

        // Google Login
        googleSignUpButton.setOnClickListener(v -> {
            startActivityForResult(
                    googleSignInClient.getSignInIntent(),
                    RC_SIGN_IN
            );
        });
    }

    // ---------------- API LOGIN ----------------
    private void loginUser() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("username", edtUsernameLogin.getText().toString().trim());
        params.put("password", edtPasswordLogin.getText().toString().trim());

        client.post(Urls.LoginUserWebService, params,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers,
                                          JSONObject response) {

                        try {
                            if (response.getString("success").equals("1")) {

                                editor.putBoolean("islogin", true);
                                editor.putString("username",
                                        edtUsernameLogin.getText().toString().trim());
                                editor.apply();

                                Toast.makeText(LoginActivity.this,
                                        "Login Successful",
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(
                                        LoginActivity.this,
                                        HomeActivity.class));
                                finish();

                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Invalid credentials",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          Throwable throwable,
                                          JSONObject errorResponse) {

                        Toast.makeText(LoginActivity.this,
                                "Server error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------- GOOGLE RESULT ----------------
    @Override
    protected void onActivityResult(int requestCode,
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

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user =
                                firebaseAuth.getCurrentUser();

                        Toast.makeText(this,
                                "Welcome " + user.getDisplayName(),
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(
                                LoginActivity.this,
                                HomeActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this,
                                "Authentication Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------- VALIDATION (FIXED) ----------------
    private boolean validateLogin() {

        String username =
                edtUsernameLogin.getText().toString().trim();
        String password =
                edtPasswordLogin.getText().toString().trim();

        if (username.isEmpty()) {
            edtUsernameLogin.setError(
                    "Enter username / mobile / email");
            return false;
        }

        // EMAIL only if looks like email
        if (username.contains("@") && username.contains(".")) {
            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(username).matches()) {
                edtUsernameLogin.setError("Invalid email address");
                return false;
            }
        }
        // MOBILE
        else if (username.matches("\\d+")) {
            if (username.length() != 10) {
                edtUsernameLogin.setError(
                        "Mobile number must be 10 digits");
                return false;
            }
        }
        // USERNAME
        else {
            if (username.length() < 3) {
                edtUsernameLogin.setError(
                        "Username must be at least 3 characters");
                return false;
            }
        }

        if (password.isEmpty()) {
            edtPasswordLogin.setError("Enter password");
            return false;
        }

        if (password.length() < 6) {
            edtPasswordLogin.setError(
                    "Password must be at least 6 characters");
            return false;
        }

        return true;
    }
}