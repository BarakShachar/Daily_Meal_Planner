package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.WriteResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserSuggestionsMenu extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomNavigationView;
    Map<String, Object> suggestion_meals = new HashMap<>();
    TableLayout table;
    PopupMenu menus;
    DocumentReference admin_ref = null;
    String user_name;
    Map<String, Object> test;
    String suggestion_menu_name;
    ArrayList<String> userExistingMenus = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_suggestions_menu);
        suggestion_menu_name = (String) getIntent().getExtras().get("suggestion_menu_name");
        String s ="morning ";
        test = new HashMap<>();
        for (int i = 0; i < 40; i++) {
            test.put(s+ Integer.toString(i), s+ Integer.toString(i));
        }
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
        getUserData();
        getUserExistingMenus();
    }

    void addMeals(){
        if (suggestion_meals.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.Table);
        for (Map.Entry<String,Object> entry : suggestion_meals.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
            Long total_cals = (Long) ((Map<String,Object>) entry.getValue()).get("total_cals");
            String menu_text = entry.getKey() + " (total calories: " + total_cals + ")";
            menu.setText(menu_text);
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
                    show_popup(view, (String) menu.getTag());
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup_list_products1(test);
                }
            });
        }
    }


    public void show_popup(View v, String selected_meal){
        menus = new PopupMenu(this,v);
        for (int i=0; i<userExistingMenus.size();i++){
            menus.getMenu().add(userExistingMenus.get(i));
        }
        menus.show();
        menus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                getMenuNameFromUser((String) menuItem.getTitle(), selected_meal);

                return true;
            }
        });
    }


    void getMenuNameFromUser(String menu_name, String selected_meal){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserSuggestionsMenu.this);
        alertDialog.setMessage("enter the meal name");
        final EditText editMeal = new EditText(UserSuggestionsMenu.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editMeal.setLayoutParams(lp);
        alertDialog.setView(editMeal);
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String meal_name = editMeal.getText().toString();
                addToUserMenu(menu_name, meal_name, (Map<String, Object>) suggestion_meals.get(selected_meal));
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

    public void popup_list_products1(Map<String,Object> products){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserSuggestionsMenu.this);
        alertDialog.setTitle("The products of the meal:");
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView = new ScrollView(UserSuggestionsMenu.this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TableLayout table = new TableLayout(this);
        for (Map.Entry<String,Object> entry : products.entrySet()) {
            TextView product = new TextView(this);
            TableRow row = new TableRow(this);
            product.setText(entry.getKey());
            row.addView(product);
            table.addView(row);
        }
        layout.addView(table);
        scrollView.addView(layout);
        rootLayout.addView(scrollView);
        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setView(rootLayout);
        alertDialog.show();

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
                        Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                        user_name = (String) document.getData().get("name");
                        admin_ref = (DocumentReference) document.getData().get("admin_ref");
                        getGeneralSuggestionMeals();
                        if (admin_ref!= null){
                            getAdminSuggestionMeals();
                        }
                        else{
                            addMeals();
                        }
                    } else {
                        Log.d("main_activity", "No such document");
                    }
                } else {
                    Log.d("main_activity", "get failed with ", task.getException());
                }
            }
        });
    }

    void getUserExistingMenus(){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("users/" + mail + "/menus")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("main_activity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userExistingMenus.add(document.getId());
                            }
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void addToUserMenu(String menu_name, String meal_name, Map<String, Object> meal_data){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Long total_add_cals = (Long) meal_data.get("total_cals");
        db.collection("users/" + mail + "/menus/" + menu_name + "/meals").document(meal_name)
                .set(meal_data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("main_activity", "user successfully written to DB!");
                        updateMenuTotalCals(menu_name, total_add_cals);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("main_activity", "Error writing user document", e);
                    }
                });
    }

    void updateMenuTotalCals(String menu_name, Long total_meal_cals){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference menuDocRef = db.collection("users/" +mail+"/menus").document(menu_name);
        menuDocRef.update("total_cals", FieldValue.increment(total_meal_cals));
    }


    void getGeneralSuggestionMeals(){
        db.collection("users/" + "Admin/" + "menus/" + suggestion_menu_name + "/meals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("main_activity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                suggestion_meals.put(document.getId(), document.getData());
                            }
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void getAdminSuggestionMeals(){
        admin_ref.collection("menus/" + suggestion_menu_name + "/meals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("main_activity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                suggestion_meals.put(document.getId(), document.getData());
                            }
                            addMeals();
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


}