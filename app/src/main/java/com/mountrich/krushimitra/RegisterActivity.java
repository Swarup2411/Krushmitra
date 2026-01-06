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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mountrich.krushimitra.Common.*;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

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

                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                registerUser();
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void registerUser() {
        //Network throught data transfer.
        //Sending and managing request over network.
        AsyncHttpClient client = new AsyncHttpClient(); // Send and manage the data.
        RequestParams params = new RequestParams();     // pass the data or data store.

        params.put("name",edtNameRegister.getText().toString()); //key and value
        params.put("mobileno",edtMobileRegister.getText().toString());
        params.put("emailid",edtEmailRegister.getText().toString());
        params.put("qualification",edtQualificationRegister.getText().toString());
        params.put("address",edtAddressRegister.getText().toString());
        params.put("username",edtUsernameRegister.getText().toString());
        params.put("password",edtPasswordRegister.getText().toString());

        client.post(Urls.RegisterUserWebServiceAddress,params,new JsonHttpResponseHandler()

        {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try
                {
                    String status = response.getString("success");

                    if (status.equals("1"))
                    {
                        Toast.makeText(RegisterActivity.this,"Registration successfully done",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);

                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this,"Username or Password is exist",Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(RegisterActivity.this,"Server Error",Toast.LENGTH_SHORT).show();
            }
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