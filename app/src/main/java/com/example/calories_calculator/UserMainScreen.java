package com.example.calories_calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class UserMainScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    TextView hello;
    ProgressBar bar;
    TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent my_intent = getIntent();
        User user = (User) my_intent.getSerializableExtra("user_data");
        setContentView(R.layout.activity_user_main_screen);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

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

        hello = (TextView) findViewById(R.id.Hello);
        bar = (ProgressBar) findViewById(R.id.Bar);
        String name = "Hello "+ user.getName();
            hello.setText(name);
        List<String> list=new ArrayList<String>();
        //Adding elements in the List
            list.add("Mango");
            list.add("Apple");
            list.add("Banana");
            list.add("Grapes");
            list.add("Mango");
            list.add("Apple");
            list.add("Banana");
            list.add("Grapes");
            list.add("Mango");
            list.add("Apple");
            list.add("Banana");
            list.add("Grapes");
            list.add("Mango");
            list.add("Apple");
            list.add("Banana");
            list.add("Grapes");
            list.add("Mango");
            list.add("Apple");
            list.add("Banana");
            list.add("Grapes");
            list.add("Mango");
            list.add("Apple");
            list.add("Banana");
            list.add("Grapes");
            list.add("Mango");
            list.add("Apple");
            list.add("Banana");
            list.add("Grapes");
        addMenus(list);
    }

    void addMenus(List<String> menus){
        int amount = menus.size();
        table = (TableLayout) findViewById(R.id.Table);
        for(int i =0; i<amount; i++){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setText(menus.get(i));
            menu.setId(i);
            menu.setGravity(Gravity.CENTER);
            menu.setTextSize(10);
            menu.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    System.out.println("v.getid is:- " + v.getId());
                }
            });
            row.addView(menu);
        }
    }
}