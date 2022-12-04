package com.example.calories_calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.HashMap;
import java.util.Map;

public class UserSuggestions extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Map<String, Object> user_meals = new HashMap<>();
    TableLayout table;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_suggestions);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        user_meals.put("morning","morning1");
        addMeals();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.user_home:
                        startActivity(new Intent(getApplicationContext(),UserMainScreen.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.user_search:
                        startActivity(new Intent(getApplicationContext(),UserSearch.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.user_suggestions:
                        startActivity(new Intent(getApplicationContext(),UserSuggestions.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

    }

    void addMeals(){
        if (user_meals.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.Table);
        for (Map.Entry<String,Object> entry : user_meals.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
           // Long total_cals = (Long) ((Map<String,Object>) entry.getValue()).get("total cals");
            //String menu_text = entry.getKey() + " (total calories: " + total_cals + ")";
            //menu.setText(menu_text);
            menu.setText(entry.getKey());
            menu.setGravity(Gravity.CENTER);
            menu.setTextSize(15);
            menu.setHeight(30);
            menu.setWidth(900);
            ImageButton add= new ImageButton(this);
            add.setImageResource(R.drawable.ic_baseline_add_24);
            row.addView(menu);
            row.addView(add);
//            menuButtons.add(menu);
//            deleteButtons.add(delete);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //remove_menu((String) menu.getTag());
                    row.removeView(add);
                    row.removeView(menu);
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in;
                    in = new Intent(UserSuggestions.this, UserSuggestionsMenu.class);
//                    in.putExtra("menu_name", (String) menu.getTag());
//                    in.putExtra("user_name", user_name);
                    startActivity(in);
                    finish();
                }
            });
        }
    }
}