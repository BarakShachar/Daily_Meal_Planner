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
    ArrayList<Map<String, Object>> userProducts;

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
        addNewMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMealNameFromUser();
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
        getUserProducts();
    }

    void mainFunction(){
        System.out.println("here1");
        addName();
        addMealsProducts();
    }

    void addName(){
        hello = findViewById(R.id.Hello);
        String hello_name = "Hello "+ userName;
        hello.setText(hello_name);
    }


    void getUserProducts(){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        System.out.println(mealName);
        db.collection("users/" + mail + "/menus/" + menuName + "/meals/").document(mealName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                                userProducts = (ArrayList<Map<String, Object>> ) document.getData().get("foods");
                                for (int i = 0; i < userProducts.size(); i++) {
                                    System.out.println("I'm in");
                                    DocumentReference food = (DocumentReference) userProducts.get(i).get("food_ref");
                                    if (i == userProducts.size() - 1) {
                                        is_last = true;
                                    }
                                    getProductsCalories(food.getId(),i);
                                }
                            }
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void getProductsCalories(String item, int place){
        DocumentReference foods = db.collection("foods").document(item);
        foods.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        System.out.println("here2");
                        Map<String,Object> product = document.getData();
                        userProducts.get(place).put("calories",product.get("calories"));
                        if (is_last){
                            mainFunction();
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
            System.out.println("empty");
            return;
        }
        table = (TableLayout) findViewById(R.id.mealTable);
        for (int i =0 ; i<userProducts.size(); i++){
            HashMap<String,Object> product = (HashMap<String,Object>)userProducts.get(i);
            TableRow row = new TableRow(this);
            TextView name = new TextView(this);
            TextView amount = new TextView(this);
            TextView calories = new TextView(this);
            System.out.println(name);

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
//            meal.setTextSize(15);
//            meal.setHeight(30);
//            meal.setWidth(900);
            ImageButton delete= new ImageButton(this);
            delete.setImageResource(R.drawable.ic_menu_delete);
            row.addView(name);
            row.addView(calories);
            row.addView(amount);
            row.addView(delete);
            deleteButtons.add(delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeMeal((String) name.getText());
                    row.removeView(delete);
                    row.removeView(name);
                }
            });
            table.addView(row);
        }

    }
    void getMealNameFromUser(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserAddProductToMeal.this);
        alertDialog.setMessage("enter meal name");
        final EditText editMeal = new EditText(UserAddProductToMeal.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editMeal.setLayoutParams(lp);
        alertDialog.setView(editMeal);
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String meal_name = editMeal.getText().toString().toLowerCase(Locale.ROOT);
                if (userMeals.containsKey(meal_name)){
                    dialogInterface.dismiss();
                }
                else {
                    removeExistingMeals();
                    addNewMeal(meal_name);
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

    void removeExistingMeals(){
        for (int i=0; i<mealButtons.size(); i++){
            ViewGroup layout = (ViewGroup) mealButtons.get(i).getParent();
            if(null!=layout) //for safety only  as you are doing onClick
                layout.removeView(mealButtons.get(i));
        }
        for (int i=0; i<deleteButtons.size(); i++){
            ViewGroup layout = (ViewGroup) deleteButtons.get(i).getParent();
            if(null!=layout) //for safety only  as you are doing onClick
                layout.removeView(deleteButtons.get(i));
        }
        mealButtons.clear();
        deleteButtons.clear();
        userMeals.clear();
    }


    void removeMeal(String meal_name){
        Long total_meal_cals = ((Long) userMeals.get("total_cals")) * -1;
        db.collection("users/" + mail + "/menus/" + mealName + "/meals").document(meal_name)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("main_activity", "DocumentSnapshot successfully deleted!");
                        updateMenuTotalCals(total_meal_cals);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("main_activity", "Error deleting document", e);
                    }
                });
    }

    void updateMenuTotalCals(Long total_meal_cals){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference menuDocRef = db.collection("users/" +mail+"/menus").document(mealName);
        menuDocRef.update("total_cals", FieldValue.increment(total_meal_cals));
    }

    void addNewMeal(String meal_name){
        Map<String, Object> meal = new HashMap<>();
        meal.put("total_cals", 0);
        db.collection("users/" + mail + "/menus/" + mealName + "/meals").document(meal_name)
                .set(meal)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("main_activity", "user successfully written to DB!");
                        getUserProducts();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("main_activity", "Error writing user document", e);
                    }
                });
    }
}