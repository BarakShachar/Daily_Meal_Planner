package com.example.calories_calculator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.HashMap;
import java.util.Map;

public class UserSuggestionsMenu extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Map<String, Object> user_meals = new HashMap<>();
    TableLayout table;
    PopupMenu menus;
    Map<String, Object> test;
    ListPopupWindow listPopupWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_suggestions_menu);
        String s ="morning ";
        test = new HashMap<>();
        for (int i = 0; i < 40; i++) {
            test.put(s+ Integer.toString(i), s+ Integer.toString(i));
        }
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        user_meals.put("morning2","morning1");
        addMeals();
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

    void addMeals(){
        if (user_meals.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.Table);
        for (Map.Entry<String,Object> entry : user_meals.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
            // Long total_cals = (Long) ((Map<String,Object>) entry.getValue()).get("total cals");
            //String menu_text = entry.getKey() + " (total calories: " + total_cals + ")";
            //menu.setText(menu_text);
            menu.setText(entry.getKey());
            menu.setGravity(Gravity.CENTER);
            menu.setTextSize(15);
            menu.setHeight(30);
            menu.setWidth(900);
            ImageButton add= new ImageButton(this);
            add.setImageResource(R.drawable.ic_baseline_add_24);
            row.addView(menu);
            row.addView(add);
//            menuButtons.add(menu);
//            deleteButtons.add(delete);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    show_popup(view);
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in;
                    String s = "morning ";
                    popup_list_products1(test);
//                    in = new Intent(UserSuggestionsMenu.this, UserSuggestions.class);
////                    in.putExtra("menu_name", (String) menu.getTag());
////                    in.putExtra("user_name", user_name);
//                    startActivity(in);
//                    finish();
                }

            });
        }
    }


    public void show_popup(View v){
        menus = new PopupMenu(this,v);
        menus.getMenu().add("AGIL");
        menus.getMenu().add("AGILarasan");
        menus.getMenu().add("Arasan");
        menus.show();
        menus.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                String s = (String) menuItem.getTitle();
                System.out.println(menuItem.getTitle());
                getMenuNameFromUser((String) menuItem.getTitle());

                return true;
            }
        });
    }


    void getMenuNameFromUser(String meal){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserSuggestionsMenu.this);
        alertDialog.setMessage("enter the meal you want to add to " + meal);
        final EditText editMenu = new EditText(UserSuggestionsMenu.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editMenu.setLayoutParams(lp);
        alertDialog.setView(editMenu);
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String menu_name = editMenu.getText().toString();
                System.out.println(menu_name);
//                if (user_menus.containsKey(menu_name)){
//                    dialogInterface.dismiss();
//                }
//                else {
//                    removeExistingMenus();
//                    addNewMenu(menu_name);
//                }
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

    public void popup_list_products1(Map<String,Object> products){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserSuggestionsMenu.this);
        alertDialog.setMessage("The products of the meal:");
        ScrollView scroll = new ScrollView(alertDialog.getContext());
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT
//        );
        //scroll.setLayoutParams(lp);
        TableLayout table = new TableLayout(scroll.getContext());
        scroll.addView(table);
        for (Map.Entry<String,Object> entry : products.entrySet()) {
            TextView product = new TextView(scroll.getContext());
            TableRow row = new TableRow(table.getContext());
            product.setText(entry.getKey());
            row.addView(product);
            table.addView(row);

        }

       // Window view= ((AlertDialog.Builder)alertDialog).create().getWindow();
        //view.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setView(scroll);
        alertDialog.show();



    }

//    public void popup_list_products(Map<String,Object> products){
//        WheelViewDialog dialog = new WheelViewDialog(this);
//
//    }


}