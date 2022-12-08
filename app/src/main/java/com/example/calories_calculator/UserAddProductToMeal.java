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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserAddProductToMeal extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> userMeals = new HashMap<>();
    ArrayList<Button> mealButtons = new ArrayList<>();
    ArrayList<ImageButton> deleteButtons = new ArrayList<>();
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addNewMeal;
    boolean is_last = false;
    TextView hello;
    TableLayout table;
    String userName; // the user name from previous screen
    String mealName; // from the previous screen
    String menuName; // the user name from previous screen
    String mail;
    ArrayList<Map<String, Object>> userProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add_product_to_meal);
        mealName = (String) getIntent().getExtras().get("meal_name");
        menuName = (String) getIntent().getExtras().get("menu_name");
        userName = (String) getIntent().getExtras().get("user_name");
        addNewMeal = findViewById(R.id.addNewMeal);
        mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
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
        getUserProducts();
    }

    void getUserProducts(){
        db.collection("users/" + mail + "/menus/" + menuName + "/meals/").document(mealName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                                ArrayList<Map<String, Object>> mealProducts = (ArrayList<Map<String, Object>>) document.getData().get("foods");
                                for (int i = 0; i < mealProducts.size(); i++) {
                                    DocumentReference food = (DocumentReference) mealProducts.get(i).get("food_ref");
                                    getProductsCalories(food, mealProducts.get(i), mealProducts.size());
                                }
                            }
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void getProductsCalories(DocumentReference foods, Map<String, Object> mealItem, int totalItems){
        foods.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String,Object> product = document.getData();
                        mealItem.put("calories",product.get("calories"));
                        userProducts.add(mealItem);
                        if (userProducts.size() == totalItems){
                            addMealsProducts();
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

    void addMealsProducts(){
        if (userProducts.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.mealTable);
        for (int i =0 ; i<userProducts.size(); i++){
            HashMap<String,Object> product = (HashMap<String,Object>)userProducts.get(i);
            TableRow row = new TableRow(this);
            TextView name = new TextView(this);
            TextView amount = new TextView(this);
            TextView calories = new TextView(this);

            name.setText(((DocumentReference)product.get("food_ref")).getId());
            Long amount_ = (Long) product.get("quantity");
            Long calories_ = ((Long) product.get("calories"))*amount_;
            amount.setText(Long.toString(amount_));
            calories.setText(Long.toString(calories_));
            amount.setGravity(Gravity.CENTER);
            amount.setTextSize(15);
            name.setGravity(Gravity.CENTER);
            name.setTextSize(15);
            calories.setGravity(Gravity.CENTER);
            calories.setTextSize(15);
            ImageButton delete= new ImageButton(this);
            delete.setImageResource(R.drawable.ic_menu_delete);
            row.addView(name);
            row.addView(calories);
            row.addView(amount);
            row.addView(delete);
            deleteButtons.add(delete);
            ImageButton edit= new ImageButton(this);
            edit.setImageResource(R.drawable.ic_edit);
            table.addView(row);
            row.addView(edit);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeProduct((String) name.getText(),amount_,calories_);
                    row.removeView(delete);
                    row.removeView(name);
                    row.removeView(amount);
                    row.removeView(calories);
                }
            });
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editAmount((String) name.getText(),amount_,(Long) product.get("calories"));
                    row.removeView(delete);
                    row.removeView(name);
                    row.removeView(amount);
                    row.removeView(calories);
                }
            });

        }

    }

    void editAmount(String productName, Long oldAmount,Long calories){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserAddProductToMeal.this);
        alertDialog.setMessage("Enter amount");
        final EditText editMenu = new EditText(UserAddProductToMeal.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editMenu.setLayoutParams(lp);
        alertDialog.setView(editMenu);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String amount = editMenu.getText().toString().toLowerCase(Locale.ROOT);
                if (Integer.parseInt(amount) ==0){
                    dialogInterface.dismiss();
                    removeProduct(productName,oldAmount,calories);
                }
                else {
                    addAmountProduct(productName,Long.parseLong(amount),oldAmount,calories);
                }
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

    void addAmountProduct(String productName,Long new_amount,Long old_amount, Long calories){
        DocumentReference docRef = db.collection("users/"+mail+"/menus/"+menuName+"/meals").document(mealName);
        DocumentReference itemRef = db.collection("foods").document(productName);
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("food_ref", itemRef);
        newItem.put("quantity",old_amount.intValue());
        docRef.update("foods", FieldValue.arrayRemove(newItem));
        newItem.clear();
        newItem.put("food_ref", itemRef);
        newItem.put("quantity", new_amount.intValue());
        docRef.update("foods", FieldValue.arrayUnion(newItem));
        Long caloriesPerOne = calories/old_amount;
        Long totalAddCals = caloriesPerOne*new_amount - calories;
        docRef.update("total_cals", FieldValue.increment(totalAddCals));
        docRef.getParent().getParent().update("total_cals", FieldValue.increment(totalAddCals));
        Toast.makeText(UserAddProductToMeal.this, productName +"updated to " + new_amount + " in your meal", Toast.LENGTH_SHORT).show();
        userProducts.clear();
        getUserProducts();
    }

    void removeProduct(String productName,Long old_amount, Long calories){
        DocumentReference docRef = db.collection("users/"+mail+"/menus/"+menuName+"/meals").document(mealName);
        DocumentReference itemRef = db.collection("foods").document(productName);
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("food_ref", itemRef);
        newItem.put("quantity",old_amount.intValue());
        docRef.update("foods", FieldValue.arrayRemove(newItem));
        Long totalAddCals = (-1)*calories;
        docRef.update("total_cals", FieldValue.increment(totalAddCals));
        docRef.getParent().getParent().update("total_cals", FieldValue.increment(totalAddCals));
        Toast.makeText(UserAddProductToMeal.this, old_amount + " " +productName +" removed from your meal", Toast.LENGTH_SHORT).show();
        userProducts.clear();
        getUserProducts();
        //no need to load from start
    }
}