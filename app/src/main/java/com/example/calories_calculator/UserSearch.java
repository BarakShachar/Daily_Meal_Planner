package com.example.calories_calculator;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserSearch extends AppCompatActivity implements View.OnClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomNavigationView;
    Button logout, vegetables, fruits, dairy;
    TableLayout table;
    ArrayList<Button> productsButton = new ArrayList<>();
    ArrayList<ImageButton> addButtons = new ArrayList<>();
    Map<String, HashMap<String, Object>> products = new HashMap<>();

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
    }

    void getProducts(String item){
        DocumentReference foods = db.collection("foods").document(item);
        foods.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        products.put(item,(HashMap<String, Object>)document.getData());
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
                                getProducts(name);
                            }

                            System.out.println("hey");
                            showProducts();

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
            System.out.println(products.isEmpty());
            System.out.println("empty");
            return;
        }
        table = (TableLayout) findViewById(R.id.product_table);
        for (Map.Entry<String, HashMap<String, Object>> entry : products.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button p = new Button(this);
            p.setTag(entry.getKey());
            //Map<String, Object> foodProducts = (Map<String, Object>) ( entry.getValue());
            Long calories = (Long)entry.getValue().get("calories");// .get("calories");
            System.out.println(calories);
            String meal_text = entry.getKey() + " (calories: " + calories + ")";
            p.setText(meal_text);
            p.setGravity(Gravity.CENTER);
            p.setTextSize(15);
            p.setHeight(30);
            p.setWidth(900);
            ImageButton add= new ImageButton(this);
            add.setImageResource(R.drawable.ic_baseline_add_24);
            row.addView(p);
            row.addView(add);
            productsButton.add(p);
            addButtons.add(add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
        }
    }

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
                System.out.println(products.isEmpty());
                getProductsRef(text);
                System.out.println(products.isEmpty());
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
}