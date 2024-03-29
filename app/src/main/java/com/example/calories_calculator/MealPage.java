package com.example.calories_calculator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MealPage extends AppCompatActivity {
    FirestoreWrapper.UserAddProductToMealWrapper wrapper = new FirestoreWrapper.UserAddProductToMealWrapper(this);
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
        setContentView(R.layout.activity_meal_page);
        mealName = (String) getIntent().getExtras().get("mealName");
        menuName = (String) getIntent().getExtras().get("menuName");
        userName = (String) getIntent().getExtras().get("userName");
        mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
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
                        in = new Intent(getApplicationContext(), Products.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0,0);
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
        wrapper.getUserProducts();
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
            row.addView(amount);
            row.addView(calories);
            row.addView(delete);
            deleteButtons.add(delete);
            ImageButton edit= new ImageButton(this);
            edit.setImageResource(R.drawable.ic_edit);
            row.addView(edit);
            table.addView(row);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    wrapper.removeProduct((String) name.getText(),amount_,calories_);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MealPage.this);
        alertDialog.setMessage("Enter amount");
        final EditText editMenu = new EditText(MealPage.this);
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
                    wrapper.removeProduct(productName,oldAmount,calories);

                }
                else {
                    wrapper.addAmountProduct(productName,Long.parseLong(amount),oldAmount,calories);
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
}