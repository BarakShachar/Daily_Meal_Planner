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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class UserMainScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
    FloatingActionButton addNewMenu = findViewById(R.id.addNewMenu);
    TextView hello;
    ProgressBar bar;
    TableLayout table;
    Map<String, Object> user_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_screen);
//        bottomNavigationView = findViewById(R.id.bottomNavigation);
        addNewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: add
            }
        });
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
        String user_mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        getUserData(user_mail);
    }

    void mainFunction(){
        hello = findViewById(R.id.Hello);
        bar = findViewById(R.id.Bar);
        String hello_name = "Hello "+ user_data.get("name");
        hello.setText(hello_name);
        if (((Map<String, Object>)user_data.get("menus")).size() > 0) {
            addMenus((Map<String, Object>)user_data.get("menus"));
        }
    }

    void addMenus(Map<String, Object> user_menus_data){
        table = (TableLayout) findViewById(R.id.Table);
        for (Map.Entry<String,Object> entry : user_menus_data.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
            Long total_cals = (Long) ((Map<String,Object>) entry.getValue()).get("total cals");
            String menu_text = entry.getKey() + " (total calories: " + Long.toString(total_cals) + ")";
            menu.setText(menu_text);
            menu.setGravity(Gravity.CENTER);
            menu.setTextSize(15);
            menu.setHeight(30);
            menu.setWidth(900);
            ImageButton delete= new ImageButton(this);
            delete.setImageResource(R.drawable.ic_menu_delete);
            row.addView(menu);
            row.addView(delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(null!=row) { //for safety only  as you are doing onClick
                        row.removeView(delete);
                        row.removeView(menu);
                    }
                    else {
                        System.out.println("here");
                    }
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    System.out.println("v.getid is:- " + v.getId());
                }
            });
        }
    }

    void getUserData(String mail){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(mail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                        user_data = document.getData();
                        mainFunction();
                    } else {
                        Log.d("main_activity", "No such document");
                    }
                } else {
                    Log.d("main_activity", "get failed with ", task.getException());
                }
            }
        });
    }

    void remove_menu(String menu_name){

    }
}