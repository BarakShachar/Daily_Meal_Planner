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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_suggestions);
        logout = findViewById(R.id.logOut);
        logout.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        logout.setOnClickListener(v -> Logout());
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
        getGeneralSuggestionMenus();
        getUserData();
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
            menu.setTextSize(15);
            menu.setHeight(30);
            menu.setWidth(900);
            ImageButton add= new ImageButton(this);
            add.setImageResource(R.drawable.ic_baseline_add_24);
            row.addView(menu);
            row.addView(add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getMenuNameFromUser((String) menu.getTag());
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in;
                    in = new Intent(UserSuggestions.this, UserSuggestionsMenu.class);
                    in.putExtra("suggestionMenuName", (String) menu.getTag());
                    startActivity(in);
                    finish();
                }
            });
        }
    }

    void getMenuNameFromUser(String selectedMenu){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserSuggestions.this);
        alertDialog.setMessage("enter the menu name");
        final EditText editMeal = new EditText(UserSuggestions.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editMeal.setLayoutParams(lp);
        alertDialog.setView(editMeal);
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String menuName = editMeal.getText().toString();
                addToUserMenu(menuName, (Map<String, Object>) suggestionMenus.get(selectedMenu));
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    void addToUserMenu(String menuName, Map<String, Object> mealData) {
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("users/" + mail + "/menus/").document(menuName)
                .set(mealData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("mainActivity", "user successfully written to DB!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("mainActivity", "Error writing user document", e);
                    }
                });
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
        db.collection("users/" + "Admin/" + "menus")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("mainActivity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                suggestionMenus.put(document.getId(), document.getData());
                            }
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

    public void Logout() {
        Intent intent = new Intent(this, Login.class);
        FirebaseAuth.getInstance().signOut();
        startActivity(intent);
        finish();
    }

}