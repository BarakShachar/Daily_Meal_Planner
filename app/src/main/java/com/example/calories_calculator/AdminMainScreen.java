package com.example.calories_calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AdminMainScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    AdminHome adminHome = new AdminHome();
    AdminEdit adminEdit= new AdminEdit();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main_screen);

        bottomNavigationView = findViewById(R.id.AdminBottomNavigation);

        // replace the main screen to the home screen while connecting to the page.
        getSupportFragmentManager().beginTransaction().replace(R.id.user_frame,adminHome).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.admin_users:
                        getSupportFragmentManager().beginTransaction().replace(R.id.user_frame, adminHome).commit();
                        return true;
                    case R.id.admin_edit:
                        getSupportFragmentManager().beginTransaction().replace(R.id.user_frame, adminEdit).commit();
                        return true;
                }
                return false;
            }
        });


    }
}