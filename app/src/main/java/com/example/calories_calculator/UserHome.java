package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class UserHome extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    TextView hello;
    ProgressBar bar;
    TableLayout table;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_home);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // replace the main screen to the home screen while connecting to the page.
        bottomNavigationView.setSelectedItemId(R.id.user_home);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.user_home:
                        startActivity(new Intent(getApplicationContext(),UserHome.class));
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










//        Intent intent = getIntent();
//        String menu = intent.getStringExtra("menu");
//        String lName = intent.getStringExtra("lastName");
        hello = (TextView) findViewById(R.id.Hello);
        bar = (ProgressBar) findViewById(R.id.Bar);
        String name = "Hello"+"Sabrina";
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
    void changeInProgress(boolean inProgress){
        if(inProgress){
            bar.setVisibility(View.VISIBLE);
        }else{
            bar.setVisibility(View.GONE);
        }
    }
    public void connect() {
        Intent in = new Intent(this, Login.class);
        startActivity(in);
    }
}