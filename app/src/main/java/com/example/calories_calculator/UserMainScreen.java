package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class UserMainScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    UserHome userHome = new UserHome();
    UserSearch userSearch = new UserSearch();
    UserSuggestions userSuggestions = new UserSuggestions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_screen);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // replace the main screen to the home screen while connecting to the page.
        getSupportFragmentManager().beginTransaction().replace(R.id.user_frame,userHome).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.user_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.user_frame, userHome).commit();
                        return true;
                    case R.id.user_search:
                        getSupportFragmentManager().beginTransaction().replace(R.id.user_frame, userSearch).commit();
                        return true;
                    case R.id.user_suggestions:
                        getSupportFragmentManager().beginTransaction().replace(R.id.user_frame, userSuggestions).commit();
                        return true;
                }
                return false;
            }
        });



    }
}