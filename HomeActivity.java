package com.example.myandroidproject.customer.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.NavHostController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myandroidproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    static final int supportId = R.id.support;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navbar_host);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView navbar = findViewById(R.id.navbar);
        navbar.setOnItemSelectedListener(item -> {

           if (item.getItemId() == R.id.support){
               navController.navigate(R.id.action_global_support_fragment);
           } else if (item.getItemId() == R.id.home) {
               navController.navigate(R.id.action_global_home);
           }

            return true;
        });

    }


}