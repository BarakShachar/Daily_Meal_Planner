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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

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
    Button vegetables, fruits, dairy, meatAndFish,cereal, breads ;
    TableLayout table;
    PopupMenu menus;
    ArrayList<Button> productsButton = new ArrayList<>();
    ArrayList<ImageButton> addButtons = new ArrayList<>();
    Map<String, HashMap<String, Object>> products = new HashMap<>();
    Map<String, ArrayList<String>> userExistingMeals = new HashMap<>();
    String userMail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    String lastSearch;
    boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_search);
        vegetables = findViewById(R.id.vegetables);
        vegetables.setOnClickListener(this);
        fruits = findViewById(R.id.fruits);
        fruits.setOnClickListener(this);
        dairy = findViewById(R.id.dairy);
        dairy.setOnClickListener(this);
        meatAndFish = findViewById(R.id.meat_and_fish);
        meatAndFish.setOnClickListener(this);
        cereal = findViewById(R.id.cereal);
        cereal.setOnClickListener(this);
        breads = findViewById(R.id.breads);
        breads.setOnClickListener(this);

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
        getUserExistingMenus();
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

    void getProducts(String item, int totalProducts){
        DocumentReference foods = db.collection("foods").document(item);
        foods.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        products.put(item,(HashMap<String, Object>)document.getData());
                        if (products.size() == totalProducts){
                            showProducts();
                        }
                        Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("mainActivity", "No such document");
                    }
                } else {
                    Log.d("mainActivity", "get failed with ", task.getException());
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
                        Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                        ArrayList<DocumentReference> foodList = (ArrayList<DocumentReference>) document.getData().get("foods");
                            for (int i =0; i< foodList.size();i++){
                                String name = foodList.get(i).getId();
                                    getProducts(name, foodList.size());
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


    void showProducts(){
        if (products.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.product_table);
        table.removeAllViews();
        for (Map.Entry<String, HashMap<String, Object>> entry : products.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button product = new Button(this);
            product.setTag(entry.getKey());
            Long calories = (Long)entry.getValue().get("calories");// .get("calories");
            String mealText = entry.getKey() + " ("+ calories+ " calories)";
            product.setText(mealText);
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
        if (userExistingMeals.isEmpty()){
            menus.getMenu().add("create new menu");
            menus.show();
            menus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent in = new Intent(UserSearch.this, UserMainScreen.class);
                    in.putExtra("isAdmin", isAdmin);
                    startActivity(in);
                    return true;
                }
            });
        }
        else {
            for (Map.Entry<String, ArrayList<String>> entry : userExistingMeals.entrySet()) {
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
    }

    void selectMealToAddItem(View view, String menuSelected, String itemName){
        menus = new PopupMenu(this,view);
        if (userExistingMeals.get(menuSelected).isEmpty()){
            menus.getMenu().add("create new meal");
            menus.show();
            menus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent in = new Intent(UserSearch.this, MenuPage.class);
                    in.putExtra("mainActivity", menuSelected);
                    in.putExtra("isAdmin", isAdmin);
                    startActivity(in);
                    finish();
                    return true;
                }
            });
        }
        else {
            for (int i = 0; i < userExistingMeals.get(menuSelected).size(); i++) {
                menus.getMenu().add(userExistingMeals.get(menuSelected).get(i));
            }
            menus.show();
            menus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getItemQuantity(menuSelected, (String) menuItem.getTitle(), itemName);
                    return true;
                }
            });
        }
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
                validateItemOnMeal(menuName, mealName, itemName, quantity);
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


    void validateItemOnMeal(String menuName, String mealName, String itemName, int quantity){
        DocumentReference itemRef = db.collection("foods").document(itemName);
        DocumentReference docRef = db.collection("users/"+userMail+"/menus/"+menuName+"/meals").document(mealName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                        ArrayList<Map<String, Object>> foodList = (ArrayList<Map<String, Object>>) document.getData().get("foods");
                        for (int i =0; i< foodList.size();i++){
                            if (itemRef.equals(foodList.get(i).get("foodRef"))){
                                Toast.makeText(UserSearch.this, "you already have "+ itemName + " in this meal", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        addItemToMeal(docRef, itemName, quantity);
                    } else {
                        Log.d("mainActivity", "No such document");
                    }
                } else {
                    Log.d("mainActivity", "get failed with ", task.getException());
                }
            }
        });
    }

    void addItemToMeal(DocumentReference docRef, String itemName, int quantity){
        DocumentReference itemRef = db.collection("foods").document(itemName);
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("foodRef", itemRef);
        newItem.put("quantity", quantity);
        docRef.update("foods", FieldValue.arrayUnion(newItem));
        Long totalAddCals = ((Long) products.get(itemName).get("calories")) * quantity;
        docRef.update("totalCals", FieldValue.increment(totalAddCals));
        docRef.getParent().getParent().update("totalCals", FieldValue.increment(totalAddCals));
        Toast.makeText(UserSearch.this, quantity + " " +itemName +" added to your meal", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        String text;
        switch (v.getId()){
            case R.id.vegetables:
                text = "vegetable";
                if (text != lastSearch){
                    products.clear();
                    lastSearch = text;
                    getProductsRef(text);
                }
                break;

            case R.id.fruits:
                text = "fruits";
                if (text != lastSearch) {
                    products.clear();
                    lastSearch = text;
                    getProductsRef(text);
                }
                break;

            case R.id.dairy:
                text = "milk & dairy";
                if (text != lastSearch) {
                    products.clear();
                    lastSearch = text;
                    getProductsRef(text);
                }
                    break;

            case R.id.meat_and_fish:
                text = "meat";
                if (text != lastSearch) {
                    products.clear();
                    lastSearch = text;
                    getProductsRef(text);
                }
                    break;

            case R.id.cereal:
                text = "cereal";
                if (text != lastSearch) {
                    products.clear();
                    lastSearch = text;
                    getProductsRef(text);
                }
                    break;

            case R.id.breads:
                text = "bread";
                if (text != lastSearch) {
                    products.clear();
                    lastSearch = text;
                    getProductsRef(text);
                }
                    break;

        }
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
                                userExistingMeals.put(document.getId(), new ArrayList<String>());
                                getUserExistingMeals(document.getId());
                            }
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void getUserExistingMeals(String menuName){
        db.collection("users/" + userMail + "/menus/" + menuName + "/meals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("mainActivity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userExistingMeals.get(menuName).add(document.getId());
                            }
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}