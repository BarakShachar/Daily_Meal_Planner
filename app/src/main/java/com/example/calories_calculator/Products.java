package com.example.calories_calculator;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Products extends AppCompatActivity implements View.OnClickListener {
    FirestoreWrapper.UserSearchWrapper wrapper = new FirestoreWrapper.UserSearchWrapper(this);
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
        setContentView(R.layout.fragment_products);
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
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Intent in;
                switch(item.getItemId()) {
                    case R.id.user_home:
                        in = new Intent(getApplicationContext(), MainPage.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.user_search:
                        break;
                    case R.id.user_suggestions:
                        in = new Intent(getApplicationContext(), SuggestionsPage.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.admin_users:
                        in = new Intent(getApplicationContext(), UsersPage.class);
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
        wrapper.getUserExistingMenus();
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
                    Intent in = new Intent(Products.this, MainPage.class);
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
                    Intent in = new Intent(Products.this, MenuPage.class);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Products.this);
        alertDialog.setMessage("How much "+itemName + " you want to add?");
        final EditText editQuantity = new EditText(Products.this);
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
                wrapper.validateItemOnMeal(menuName, mealName, itemName, quantity);
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




    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        String text;
        switch (v.getId()){
            case R.id.vegetables:
                text = "vegetable";
                if (!text.equals(lastSearch)){
                    products.clear();
                    lastSearch = text;
                    wrapper.getProductsRef(text);
                }
                break;

            case R.id.fruits:
                text = "fruits";
                if (!text.equals(lastSearch)) {
                    products.clear();
                    lastSearch = text;
                    wrapper.getProductsRef(text);
                }
                break;

            case R.id.dairy:
                text = "milk & dairy";
                if (!text.equals(lastSearch)) {
                    products.clear();
                    lastSearch = text;
                    wrapper.getProductsRef(text);
                }
                    break;

            case R.id.meat_and_fish:
                text = "meat";
                if (!text.equals(lastSearch)) {
                    products.clear();
                    lastSearch = text;
                    wrapper.getProductsRef(text);
                }
                    break;

            case R.id.cereal:
                text = "cereal";
                if (!text.equals(lastSearch)) {
                    products.clear();
                    lastSearch = text;
                    wrapper.getProductsRef(text);
                }
                    break;

            case R.id.breads:
                text = "bread";
                if (!text.equals(lastSearch)) {
                    products.clear();
                    lastSearch = text;
                    wrapper.getProductsRef(text);
                }
                    break;

        }
    }

}