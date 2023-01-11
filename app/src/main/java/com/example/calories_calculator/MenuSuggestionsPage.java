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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenuSuggestionsPage extends AppCompatActivity {
    FirestoreWrapper.UserSuggestionsMenuWrapper wrapper = new FirestoreWrapper.UserSuggestionsMenuWrapper(this);
    BottomNavigationView bottomNavigationView;
    Map<String, Object> suggestionMeals = new HashMap<>();
    TableLayout table;
    PopupMenu menus;
    DocumentReference adminRef = null;
    String userName;
    String suggestionMenuName;
    ArrayList<String> userExistingMenus = new ArrayList<>();
    String userMail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_suggestions_page);
        suggestionMenuName = (String) getIntent().getExtras().get("suggestionMenuName");
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
        wrapper.getGeneralSuggestionMeals();
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

    void addMeals(){
        if (suggestionMeals.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.Table);
        for (Map.Entry<String,Object> entry : suggestionMeals.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
            Long totalCals = (Long) ((Map<String,Object>) entry.getValue()).get("totalCals");
            String menuText = entry.getKey() + " (total calories: " + totalCals + ")";
            menu.setText(menuText);
            menu.setGravity(Gravity.CENTER);
            menu.setTextSize(15);
            menu.setHeight(30);
            menu.setWidth(950);
            ImageButton add= new ImageButton(this);
            add.setImageResource(R.drawable.ic_baseline_add_24);
            row.addView(menu);
            row.addView(add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopup(view, (String) menu.getTag());
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupListProducts((String) menu.getTag());
                }
            });
        }
    }

    public void showPopup(View v, String selectedMeal){
        menus = new PopupMenu(this,v);
        for (int i=0; i<userExistingMenus.size();i++){
            menus.getMenu().add(userExistingMenus.get(i));
        }
        menus.show();
        menus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                getMealNameFromUser((String) menuItem.getTitle(), selectedMeal);

                return true;
            }
        });
    }

    void getMealNameFromUser(String menuName, String selectedMeal){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuSuggestionsPage.this);
        alertDialog.setMessage("enter the meal name");
        final EditText editMeal = new EditText(MenuSuggestionsPage.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editMeal.setLayoutParams(lp);
        alertDialog.setView(editMeal);
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String mealName = editMeal.getText().toString();
                wrapper.addToUserMenu(menuName, mealName, (Map<String, Object>) suggestionMeals.get(selectedMeal));
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

    public void popupListProducts(String mealName){
        ArrayList<Map<String, Object>> foodProducts = ((Map<String, ArrayList<Map<String, Object>>>) suggestionMeals.get(mealName)).get("foods");
        if (foodProducts == null){
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuSuggestionsPage.this);
        alertDialog.setTitle("The products of the meal:");
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView = new ScrollView(MenuSuggestionsPage.this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TableLayout table = new TableLayout(this);
        TextView nameHead = new TextView(this);
        TextView quantityHead = new TextView(this);
        TableRow rowHead = new TableRow(this);
        nameHead.setText("Name");
        nameHead.setTextSize(20);
        nameHead.setWidth(500);
        nameHead.setGravity(Gravity.CENTER);
        rowHead.addView(nameHead);
        quantityHead.setText("Quantity");
        quantityHead.setTextSize(20);
        quantityHead.setWidth(500);
        quantityHead.setGravity(Gravity.CENTER);
        rowHead.addView(quantityHead);
        table.addView(rowHead);
        for (Map<String, Object> currentFood : foodProducts) {
            TextView product = new TextView(this);
            TextView productQuantity = new TextView(this);
            TableRow row = new TableRow(this);
            DocumentReference foodRef = (DocumentReference) currentFood.get("foodRef");
            String foodName = foodRef.getId();
            String foodQuantity = Long.toString((Long) currentFood.get("quantity"));
            productQuantity.setText(foodQuantity);
            productQuantity.setTextSize(20);
            productQuantity.setGravity(Gravity.CENTER);
            product.setText(foodName);
            product.setTextSize(20);
            product.setGravity(Gravity.CENTER);
            row.addView(product);
            row.addView(productQuantity);
            table.addView(row);
        }
        layout.addView(table);
        scrollView.addView(layout);
        rootLayout.addView(scrollView);
        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setView(rootLayout);
        alertDialog.show();

    }
}