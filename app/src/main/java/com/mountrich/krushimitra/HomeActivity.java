package com.mountrich.krushimitra;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;


import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.mountrich.krushimitra.fragments.CartFragment;
import com.mountrich.krushimitra.fragments.CropDoctorFragement;
import com.mountrich.krushimitra.fragments.HomeFragment;
import com.mountrich.krushimitra.fragments.OrdersFragment;
import com.mountrich.krushimitra.fragments.WeatherFragment;

public class HomeActivity extends AppCompatActivity {

//    MaterialToolbar toolbar;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;

     BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();

        bottomNav = findViewById(R.id.bottom_nav);

//        toolbar = findViewById(R.id.toolbar);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        bottomNav = findViewById(R.id.bottom_nav);

        // Default tab
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment fragment = null;

            if(item.getItemId() == R.id.nav_home){
                fragment = new HomeFragment();

            }

            if(item.getItemId() == R.id.nav_cart){
                fragment = new CartFragment();

            }

            if(item.getItemId() == R.id.nav_crop_doctor){
                fragment = new CropDoctorFragement();

            }

            if(item.getItemId() == R.id.nav_orders){
                fragment = new OrdersFragment();

            }

            if(item.getItemId() == R.id.nav_weather){
                fragment = new WeatherFragment();
            }


            if (fragment != null) {
                loadFragment(fragment);
            }

            return true;
        });

    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }

        if (item.getItemId() == R.id.menu_logout) {

             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle("Logout")
                             .setMessage("Are you sure you want to log out ?")
                                     .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialogInterface, int i) {
                                             logoutUser();
                                         }
                                     })
                     .setNegativeButton("No", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                         }
                     }).show();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(task -> {

            // Firebase sign out
            firebaseAuth.signOut();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }




}
