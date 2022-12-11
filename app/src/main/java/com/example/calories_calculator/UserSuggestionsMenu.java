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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserSuggestionsMenu extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomNavigationView;
    Map<String, Object> suggestionMeals = new HashMap<>();
    TableLayout table;
    PopupMenu menus;
    DocumentReference adminRef = null;
    String userName;
    String suggestionMenuName;
    ArrayList<String> userExistingMenus = new ArrayList<>();
    String userMail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_suggestions_menu);
        suggestionMenuName = (String) getIntent().getExtras().get("suggestionMenuName");
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
                        in = new Intent(getApplicationContext(),UserSuggestions.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0,0);
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
        getGeneralSuggestionMeals();
        getUserExistingMenus();
    }

    void addMeals(){
        if (suggestionMeals.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.Table);
        for (Map.Entry<String,Object> entry : suggestionMeals.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
            Long totalCals = (Long) ((Map<String,Object>) entry.getValue()).get("totalCals");
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
                    showPopup(view, (String) menu.getTag());
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupListProducts((String) menu.getTag());
                }
            });
        }
    }

    public void showPopup(View v, String selectedMeal){
        menus = new PopupMenu(this,v);
        for (int i=0; i<userExistingMenus.size();i++){
            menus.getMenu().add(userExistingMenus.get(i));
        }
        menus.show();
        menus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                getMealNameFromUser((String) menuItem.getTitle(), selectedMeal);

                return true;
            }
        });
    }

    void getMealNameFromUser(String menuName, String selectedMeal){
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
                String mealName = editMeal.getText().toString();
                addToUserMenu(menuName, mealName, (Map<String, Object>) suggestionMeals.get(selectedMeal));
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

    public void popupListProducts(String mealName){
        ArrayList<Map<String, Object>> foodProducts = ((Map<String, ArrayList<Map<String, Object>>>) suggestionMeals.get(mealName)).get("foods");
        if (foodProducts == null){
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserSuggestionsMenu.this);
        alertDialog.setTitle("The products of the meal:");
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView = new ScrollView(UserSuggestionsMenu.this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TableLayout table = new TableLayout(this);
        TextView nameHead = new TextView(this);
        TextView quantityHead = new TextView(this);
        TableRow rowHead = new TableRow(this);
        nameHead.setText("name");
        nameHead.setTextSize(20);
        nameHead.setWidth(300);
        rowHead.addView(nameHead);
        quantityHead.setText("quantity");
        quantityHead.setTextSize(20);
        rowHead.addView(quantityHead);
        table.addView(rowHead);
        for (Map<String, Object> currentFood : foodProducts) {
            TextView product = new TextView(this);
            TextView productQuantity = new TextView(this);
            TableRow row = new TableRow(this);
            DocumentReference foodRef = (DocumentReference) currentFood.get("foodRef");
            String foodName = foodRef.getId();
            String foodQuantity = Long.toString((Long) currentFood.get("quantity"));
            productQuantity.setText(foodQuantity);
            productQuantity.setTextSize(10);
            product.setText(foodName);
            product.setTextSize(10);
            row.addView(product);
            row.addView(productQuantity);
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
        DocumentReference docRef = db.collection("users").document(userMail);
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
                            getAdminSuggestionMeals();
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

    void getUserExistingMenus(){
        db.collection("users/" + userMail + "/menus")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("mainActivity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userExistingMenus.add(document.getId());
                            }
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void addToUserMenu(String menuName, String mealName, Map<String, Object> mealData){
        Long totalAddCals = (Long) mealData.get("totalCals");
        db.collection("users/" + userMail + "/menus/" + menuName + "/meals").document(mealName)
                .set(mealData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("mainActivity", "user successfully written to DB!");
                        updateMenuTotalCals(menuName, totalAddCals);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("mainActivity", "Error writing user document", e);
                    }
                });
    }

    void updateMenuTotalCals(String menuName, Long totalMealCals){
        DocumentReference menuDocRef = db.collection("users/" + userMail +"/menus").document(menuName);
        menuDocRef.update("totalCals", FieldValue.increment(totalMealCals));
    }

    void getGeneralSuggestionMeals(){
        db.collection("users/" + "admin@gmail.com/" + "menus/" + suggestionMenuName + "/meals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("mainActivity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                suggestionMeals.put(document.getId(), document.getData());
                            }
                            getUserData();
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void getAdminSuggestionMeals(){
        adminRef.collection("menus/" + suggestionMenuName + "/meals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("mainActivity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                suggestionMeals.put(document.getId(), document.getData());
                            }
                            addMeals();
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


}