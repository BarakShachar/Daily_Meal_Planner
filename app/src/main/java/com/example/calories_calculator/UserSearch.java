package com.example.calories_calculator;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.gms.tasks.OnCompleteListener;
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

public class UserSearch extends AppCompatActivity implements View.OnClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomNavigationView;
    Button logout, vegetables, fruits, dairy;
    TableLayout table;
    PopupMenu menus;
    ArrayList<Button> productsButton = new ArrayList<>();
    ArrayList<ImageButton> addButtons = new ArrayList<>();
    Map<String, HashMap<String, Object>> products = new HashMap<>();
    Map<String, ArrayList<String>> userExistingMeals = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_search);
        logout = findViewById(R.id.logOut);
        logout.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        logout.setOnClickListener(v -> Logout());
        vegetables = findViewById(R.id.vegetables);
        vegetables.setOnClickListener(this);
        fruits = findViewById(R.id.fruits);
        fruits.setOnClickListener(this);
        dairy = findViewById(R.id.dairy);
        dairy.setOnClickListener(this);

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
        getUserExistingMenus();
    }

    void getProducts(String item, boolean is_last){
        DocumentReference foods = db.collection("foods").document(item);
        foods.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        products.put(item,(HashMap<String, Object>)document.getData());
                        if (is_last){
                            showProducts();
                        }
                        Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("main_activity", "No such document");
                    }
                } else {
                    Log.d("main_activity", "get failed with ", task.getException());
                }
            }
        });
    }
    void getProductsRef(String text){
        DocumentReference docRef = db.collection("food types").document(text);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                        ArrayList<DocumentReference> foodList = (ArrayList<DocumentReference>) document.getData().get("foods");
                            for (int i =0; i< foodList.size();i++){
                                String name = foodList.get(i).getId();
                                if (i == foodList.size() - 1) {
                                    getProducts(name, true);
                                } else {
                                    getProducts(name, false);
                                }
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


    void showProducts(){
        if (products.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.product_table);
        for (Map.Entry<String, HashMap<String, Object>> entry : products.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button product = new Button(this);
            product.setTag(entry.getKey());
            Long calories = (Long)entry.getValue().get("calories");// .get("calories");
            String meal_text = entry.getKey() + " ("+ calories+ " calories)";
            product.setText(meal_text);
            product.setGravity(Gravity.CENTER);
            product.setTextSize(15);
            product.setHeight(30);
            product.setWidth(900);
            ImageButton add= new ImageButton(this);
            add.setImageResource(R.drawable.ic_baseline_add_24);
            row.addView(product);
            row.addView(add);
            productsButton.add(product);
            addButtons.add(add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMenuToAddItem(view, (String) product.getTag());
                }
            });
        }
    }

    void selectMenuToAddItem(View view, String itemName){
        menus = new PopupMenu(this,view);
        for (Map.Entry<String,ArrayList<String>> entry : userExistingMeals.entrySet()){
            menus.getMenu().add(entry.getKey());
        }
        menus.show();
        menus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                selectMealToAddItem(view, (String) menuItem.getTitle(), itemName);
                return true;
            }
        });
    }
    void selectMealToAddItem(View view, String menuSelected, String itemName){
        menus = new PopupMenu(this,view);
        for (int i=0; i<userExistingMeals.get(menuSelected).size();i++){
            menus.getMenu().add(userExistingMeals.get(menuSelected).get(i));
        }
        menus.show();
        menus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //TODO: make loading circle shit
                getItemQuantity(menuSelected, (String) menuItem.getTitle(), itemName);
                return true;
            }
        });
    }

    void getItemQuantity(String menuName, String mealName, String itemName){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserSearch.this);
        alertDialog.setMessage("How much "+itemName + " you want to add?");
        final EditText editQuantity = new EditText(UserSearch.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        //TODO: only numbers aloud
        editQuantity.setLayoutParams(lp);
        alertDialog.setView(editQuantity);
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int quantity = Integer.parseInt(editQuantity.getText().toString());
                validateAndAddNoItemOnMeal(menuName, mealName, itemName, quantity);
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


    void validateAndAddNoItemOnMeal(String menuName, String mealName, String itemName, int quantity){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference itemRef = db.collection("foods").document(itemName);
        DocumentReference docRef = db.collection("users/"+mail+"/menus/"+menuName+"/meals").document(mealName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        boolean foodExist = false;
                        Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                        ArrayList<Map<String, Object>> foodList = (ArrayList<Map<String, Object>>) document.getData().get("foods");
                        for (int i =0; i< foodList.size();i++){
                            if (foodList.get(i).get("food_ref") == itemRef){
                                foodExist = true;
                                //TODO: shout on the user he is an idiot
                                break;
                            }
                        }
                        if (!foodExist){
                            Map<String, Object> newItem = new HashMap<>();
                            newItem.put("food_ref", itemRef);
                            newItem.put("quantity", quantity);
                            docRef.update("foods", FieldValue.arrayUnion(newItem));
                            Long totalAddCals = ((Long) products.get(itemName).get("calories")) * quantity;
                            docRef.update("total_cals", FieldValue.increment(totalAddCals));
                            docRef.getParent().getParent().update("total_cals", FieldValue.increment(totalAddCals));
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


    //if there is no meals at all
    //if there is no menus
    //if there is already this item
    //load circle
    //cache search

    public void Logout() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        String text;
        switch (v.getId()){
            case R.id.vegetables:
                text = "vegetable";
                getProductsRef(text);
                break;

            case R.id.fruits:
                text = "fruit";
                //getProductsRef(text);
                break;

            case R.id.dairy:
                text = "dairy";
                //getProductsRef(text);
                break;
        }

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
                                userExistingMeals.put(document.getId(), new ArrayList<String>());
                                getUserExistingMeals(document.getId());
                            }
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void getUserExistingMeals(String menuName){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("users/" + mail + "/menus/" + menuName + "/meals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("main_activity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userExistingMeals.get(menuName).add(document.getId());
                            }
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void getUserExistingMeals(){

    }
}