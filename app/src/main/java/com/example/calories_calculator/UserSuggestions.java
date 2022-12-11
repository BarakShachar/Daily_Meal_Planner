package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserSuggestions extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomNavigationView;
    Map<String, Object> suggestionMenus = new HashMap<>();
    TableLayout table;
    Button logout;
    DocumentReference adminRef = null;
    String userName;
    boolean isAdmin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_suggestions);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        isAdmin = (boolean) getIntent().getExtras().get("isAdmin");
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Intent in;
                switch(item.getItemId()) {
                    case R.id.user_home:
                        in = new Intent(getApplicationContext(),UserMainScreen.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.user_search:
                        in = new Intent(getApplicationContext(),UserSearch.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.user_suggestions:
                        break;
                    case R.id.admin_users:
                        in = new Intent(getApplicationContext(),AdminMainScreen.class);
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
        getGeneralSuggestionMenus();
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
                    in = new Intent(UserSuggestions.this, UserSuggestionsMenu.class);
                    in.putExtra("suggestionMenuName", (String) menu.getTag());
                    in.putExtra("isAdmin", isAdmin);
                    startActivity(in);
                    finish();
                }
            });
        }
    }

    void getUserData(){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference docRef = db.collection("users").document(mail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                        userName = (String) document.getData().get("name");
                        adminRef = (DocumentReference) document.getData().get("adminRef");
                        if (adminRef != null){
                            getAdminSuggestionMenus();
                        }
                        else{
                            addMeals();
                        }
                    } else {
                        Log.d("mainActivity", "No such document");
                    }
                } else {
                    Log.d("mainActivity", "get failed with ", task.getException());
                }
            }
        });
    }

    void getGeneralSuggestionMenus(){
        db.collection("users/" + "admin@gmail.com/" + "menus")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("mainActivity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                suggestionMenus.put(document.getId(), document.getData());
                            }
                            getUserData();
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void getAdminSuggestionMenus(){
        adminRef.collection("menus")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("mainActivity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                suggestionMenus.put(document.getId(), document.getData());
                            }
                            addMeals();
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}