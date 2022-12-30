package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MenuPage extends AppCompatActivity {
    FirestoreWrapper wrapper = new FirestoreWrapper();
    Map<String, Object> userMeals = new HashMap<>();
    ArrayList<Button> mealButtons = new ArrayList<>();
    ArrayList<ImageButton> deleteButtons = new ArrayList<>();
    String userMail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addNewMeal;
    TextView meals;
    TableLayout table;
    boolean isAdmin;
    String userName; // the user name from previous screen
    String menuName; // from the previous screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_page);
        menuName = (String) getIntent().getExtras().get("menuName");
        meals = findViewById(R.id.meals);
        meals.setText(menuName + " Meals");
        addNewMeal = findViewById(R.id.addNewMeal);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        addNewMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMealNameFromUser();
            }
        });
        isAdmin = (boolean) getIntent().getExtras().get("isAdmin");
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
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
        getUserMeals();
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

    void mainFunction(){
        addMenusMeals();
    }


    void getUserMeals(){
        wrapper.getCollectionRef("users/" + userMail + "/menus/" + menuName + "/meals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userMeals.put(document.getId(), document.getData());
                            }
                            mainFunction();
                        }
                    }
                });
    }


    void addMenusMeals(){
        if (userMeals.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.mealTable);
        for (Map.Entry<String,Object> entry : userMeals.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button meal = new Button(this);
            meal.setTag(entry.getKey());
            Long totalCals = (Long) ((Map<String,Object>) entry.getValue()).get("totalCals");
            String mealText = entry.getKey() + " (total calories: " + totalCals + ")";
            meal.setText(mealText);
            meal.setGravity(Gravity.CENTER);
            meal.setTextSize(12);
            meal.setHeight(30);
            meal.setWidth(900);
            ImageButton delete= new ImageButton(this);
            delete.setImageResource(R.drawable.ic_menu_delete);
            row.addView(meal);
            row.addView(delete);
            mealButtons.add(meal);
            deleteButtons.add(delete);
            meal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent in;
                    in = new Intent(MenuPage.this, UserAddProductToMeal.class);
                    in.putExtra("userName", userName);
                    in.putExtra("mealName", entry.getKey());
                    in.putExtra("menuName", menuName);
                    in.putExtra("isAdmin", isAdmin);
                    startActivity(in);
                    finish();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeMeal((String) meal.getTag());
                    row.removeView(delete);
                    row.removeView(meal);
                }
            });

        }
    }
    void getMealNameFromUser(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuPage.this);
        alertDialog.setMessage("enter meal name");
        final EditText editMeal = new EditText(MenuPage.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editMeal.setLayoutParams(lp);
        alertDialog.setView(editMeal);
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String mealName = editMeal.getText().toString().toLowerCase(Locale.ROOT);
                if (userMeals.containsKey(mealName)){
                    dialogInterface.dismiss();
                }
                else {
                    removeExistingMeals();
                    addNewMeal(mealName);
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


    void removeMeal(String mealName){
        Long totalMealCals = ((Long) userMeals.get("totalCals")) * -1;
        wrapper.getDocumentRef("users/" + userMail + "/menus/" + menuName + "/meals/"+mealName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("mainActivity", "DocumentSnapshot successfully deleted!");
                        updateMenuTotalCals(totalMealCals);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("mainActivity", "Error deleting document", e);
                    }
                });
    }

    void updateMenuTotalCals(Long totalMealCals){
        DocumentReference menuDocRef = wrapper.getDocumentRef("users/" +userMail+"/menus/"+menuName);
        menuDocRef.update("totalCals", FieldValue.increment(totalMealCals));
    }

    void addNewMeal(String mealName){
        Map<String, Object> meal = new HashMap<>();
        ArrayList<Map<String, Object>> foods = new ArrayList<>();
        meal.put("totalCals", 0);
        meal.put("foods", foods);
        wrapper.setDocument("users/" + userMail + "/menus/" + menuName + "/meals/"+mealName, meal)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getUserMeals();
                    }
                });
    }
}