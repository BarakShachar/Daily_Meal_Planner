package com.example.calories_calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class SuggestionsPage extends AppCompatActivity {
    FirestoreWrapper.UserSuggestionsWrapper wrapper = new FirestoreWrapper.UserSuggestionsWrapper(this);
    BottomNavigationView bottomNavigationView;
    public Map<String, Object> suggestionMenus = new HashMap<>();
    TableLayout table;
    DocumentReference adminRef = null;
    String userName;
    boolean isAdmin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_suggestions_page);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        isAdmin = (boolean) getIntent().getExtras().get("isAdmin");
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Intent in;
                switch(item.getItemId()) {
                    case R.id.user_home:
                        in = new Intent(getApplicationContext(), MainPage.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.user_search:
                        in = new Intent(getApplicationContext(), Products.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.user_suggestions:
                        break;
                    case R.id.admin_users:
                        in = new Intent(getApplicationContext(), UsersPage.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                }
                return false;
            }
        });
        if (isAdmin){
            bottomNavigationView.getMenu().removeItem(R.id.user_suggestions);
        }
        else{
            bottomNavigationView.getMenu().removeItem(R.id.admin_users);
        }
        wrapper.getGeneralSuggestionMenus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
        return true;
    }

    void addMeals(){
        if (suggestionMenus.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.Table);
        for (Map.Entry<String,Object> entry : suggestionMenus.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
            Long totalCals = (Long) ((Map<String,Object>) entry.getValue()).get("total cals");
            String menuText = entry.getKey() + " (total calories: " + totalCals + ")";
            menu.setText(menuText);
            menu.setText(entry.getKey());
            menu.setGravity(Gravity.CENTER);
            menu.setTextSize(12);
            menu.setHeight(30);
            menu.setWidth(1050);
            row.addView(menu);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in;
                    in = new Intent(SuggestionsPage.this, MenuSuggestionsPage.class);
                    in.putExtra("suggestionMenuName", (String) menu.getTag());
                    in.putExtra("isAdmin", isAdmin);
                    startActivity(in);
                    finish();
                }
            });
        }
    }


}