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
    TableLayout table;
    String userName; // the user name from previous screen
    String mealName; // from the previous screen
    String menuName; // the user name from previous screen
    String mail;
    ArrayList<Map<String, Object>> userProducts = new ArrayList<>();
    boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add_product_to_meal);
        mealName = (String) getIntent().getExtras().get("mealName");
        menuName = (String) getIntent().getExtras().get("menuName");
        userName = (String) getIntent().getExtras().get("userName");
        mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
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
                                Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                                ArrayList<Map<String, Object>> mealProducts = (ArrayList<Map<String, Object>>) document.getData().get("foods");
                                for (int i = 0; i < mealProducts.size(); i++) {
                                    DocumentReference food = (DocumentReference) mealProducts.get(i).get("foodRef");
                                    getProductsCalories(food, mealProducts.get(i), mealProducts.size());
                                }
                            }
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
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

    void addMealsProducts(){
        if (userProducts.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.mealTable);
        int count = table.getChildCount();
        for (int i = 2; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }
        for (int i =0 ; i<userProducts.size(); i++){
            HashMap<String,Object> product = (HashMap<String,Object>)userProducts.get(i);
            TableRow row = new TableRow(this);
            TextView name = new TextView(this);
            TextView amount = new TextView(this);
            TextView calories = new TextView(this);

            name.setText(((DocumentReference)product.get("foodRef")).getId());
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
            row.addView(edit);
            table.addView(row);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeProduct((String) name.getText(),amount_,calories_);
                    row.removeView(delete);
                    row.removeView(name);
                    row.removeView(amount);
                    row.removeView(calories);
                    row.removeView(edit);
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
                    row.removeView(edit);

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

    void addAmountProduct(String productName,Long newAmount,Long oldAmount, Long calories){
        DocumentReference docRef = db.collection("users/"+mail+"/menus/"+menuName+"/meals").document(mealName);
        DocumentReference itemRef = db.collection("foods").document(productName);
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("foodRef", itemRef);
        newItem.put("quantity",oldAmount.intValue());
        docRef.update("foods", FieldValue.arrayRemove(newItem));
        newItem.clear();
        newItem.put("foodRef", itemRef);
        newItem.put("quantity", newAmount.intValue());
        docRef.update("foods", FieldValue.arrayUnion(newItem));
        Long caloriesPerOne = calories/oldAmount;
        Long totalAddCals = caloriesPerOne*newAmount - calories;
        docRef.update("totalCals", FieldValue.increment(totalAddCals));
        docRef.getParent().getParent().update("totalCals", FieldValue.increment(totalAddCals));
        Toast.makeText(UserAddProductToMeal.this, productName +" amount updated to " + newAmount + " in your meal", Toast.LENGTH_SHORT).show();
        userProducts.clear();
        getUserProducts();
    }

    void removeProduct(String productName,Long oldAmount, Long calories){
        DocumentReference docRef = db.collection("users/"+mail+"/menus/"+menuName+"/meals").document(mealName);
        DocumentReference itemRef = db.collection("foods").document(productName);
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("foodRef", itemRef);
        newItem.put("quantity",oldAmount.intValue());
        docRef.update("foods", FieldValue.arrayRemove(newItem));
        Long totalAddCals = (-1)*calories;
        docRef.update("totalCals", FieldValue.increment(totalAddCals));
        docRef.getParent().getParent().update("totalCals", FieldValue.increment(totalAddCals));
        Toast.makeText(UserAddProductToMeal.this, oldAmount + " " +productName +" removed from your meal", Toast.LENGTH_SHORT).show();
        userProducts.clear();
        getUserProducts();
    }
}