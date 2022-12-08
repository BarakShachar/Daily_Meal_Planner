package com.example.calories_calculator;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminEdit extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_admin_edit);
        logout = findViewById(R.id.logOut);
        logout.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        logout.setOnClickListener(v -> Logout());

        bottomNavigationView = findViewById(R.id.AdminBottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.admin_users:
                        startActivity(new Intent(getApplicationContext(), AdminMainScreen.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.admin_edit:
                        startActivity(new Intent(getApplicationContext(), AdminEdit.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }
    public void Logout() {
        Intent intent = new Intent(this, Login.class);
        FirebaseAuth.getInstance().signOut();
        startActivity(intent);
        finish();
    }
}