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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Menu_Page extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> userMeals = new HashMap<>();
    ArrayList<Button> mealButtons = new ArrayList<>();
    ArrayList<ImageButton> deleteButtons = new ArrayList<>();
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addNewMeal;
    TextView hello;
    TableLayout table;
    String user_name; // the user name from previous screen
    String menuName; // from the previous screen
    String mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_page);
        menuName = (String) getIntent().getExtras().get("menu_name");
//        user_name = (String) getIntent().getExtras().get("user_name");
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
        getUserMeals();
    }

    void mainFunction(){
//        addName();
        addMenusMeals();
    }

    void addName(){
        hello = findViewById(R.id.Hello);
        String hello_name = "Hello "+ user_name;
        hello.setText(hello_name);
    }


    void getUserMeals(){
        db.collection("users/" + mail + "/menus/" + menuName + "/meals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("main_activity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userMeals.put(document.getId(), document.getData());
                            }
                            mainFunction();
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
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
            Long total_cals = (Long) ((Map<String,Object>) entry.getValue()).get("total_cals");
            String meal_text = entry.getKey() + " (total calories: " + total_cals + ")";
            meal.setText(meal_text);
            meal.setGravity(Gravity.CENTER);
            meal.setTextSize(15);
            meal.setHeight(30);
            meal.setWidth(900);
            ImageButton delete= new ImageButton(this);
            delete.setImageResource(R.drawable.ic_menu_delete);
            row.addView(meal);
            row.addView(delete);
            mealButtons.add(meal);
            deleteButtons.add(delete);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Menu_Page.this);
        alertDialog.setMessage("enter meal name");
        final EditText editMeal = new EditText(Menu_Page.this);
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
        db.collection("users/" + mail + "/menus/" + menuName + "/meals").document(meal_name)
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
        DocumentReference menuDocRef = db.collection("users/" +mail+"/menus").document(menuName);
        menuDocRef.update("total_cals", FieldValue.increment(total_meal_cals));
    }

    void addNewMeal(String meal_name){
        Map<String, Object> meal = new HashMap<>();
        ArrayList<Map<String, Object>> foods = new ArrayList<>();
        meal.put("total_cals", 0);
        meal.put("foods", foods);
        db.collection("users/" + mail + "/menus/" + menuName + "/meals").document(meal_name)
                .set(meal)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("main_activity", "user successfully written to DB!");
                        getUserMeals();
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