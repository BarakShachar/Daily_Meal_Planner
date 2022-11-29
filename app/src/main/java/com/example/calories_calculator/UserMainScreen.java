package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserMainScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    TextView hello;
    ProgressBar bar;
    Create_user user = new Create_user();
    TableLayout table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user.setEmail("test@gmail.com");
        setContentView(R.layout.activity_user_main_screen);
        getUserData();
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
//        String name = "Hello "+ user.getName();
//            hello.setText(name);
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


    void getUserData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                        user.set_user(document.getData());
                        String name = "Hello "+ user.getName();
                        hello.setText(name);
                    } else {
                        Log.d("main_activity", "No such document");
                    }
                } else {
                    Log.d("main_activity", "get failed with ", task.getException());
                }
            }
        });
    }
}