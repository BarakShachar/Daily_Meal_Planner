package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.common.subtyping.qual.Bottom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserMainScreen extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addNewMenu;
    TextView hello;
    TableLayout table;
    Button logout;
    String user_name;
    ArrayList<Button> menuButtons = new ArrayList<>();
    ArrayList<ImageButton> deleteButtons = new ArrayList<>();
    Map<String, Object> user_menus = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_screen);
        addNewMenu = findViewById(R.id.addNewMenu);
        logout = findViewById(R.id.logOut);
        logout.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        logout.setOnClickListener(v -> Logout());

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        addNewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMenuNameFromUser();
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
        getUserName();
    }

    void mainFunction(){
        addName();
        addMenus();
    }

    void removeExistingMenus(){
        for (int i=0; i<menuButtons.size(); i++){
            ViewGroup layout = (ViewGroup) menuButtons.get(i).getParent();
            if(null!=layout) //for safety only  as you are doing onClick
                layout.removeView(menuButtons.get(i));
        }
        for (int i=0; i<deleteButtons.size(); i++){
            ViewGroup layout = (ViewGroup) deleteButtons.get(i).getParent();
            if(null!=layout) //for safety only  as you are doing onClick
                layout.removeView(deleteButtons.get(i));
        }
        menuButtons.clear();
        deleteButtons.clear();
        user_menus.clear();
    }

    void addMenus(){
        if (user_menus.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.Table);
        for (Map.Entry<String,Object> entry : user_menus.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
            Long total_cals = (Long) ((Map<String,Object>) entry.getValue()).get("total_cals");
            String menu_text = entry.getKey() + " (total calories: " + total_cals + ")";
            menu.setText(menu_text);
            menu.setGravity(Gravity.CENTER);
            menu.setTextSize(15);
            menu.setHeight(30);
            menu.setWidth(900);
            ImageButton delete= new ImageButton(this);
            delete.setImageResource(R.drawable.ic_menu_delete);
            row.addView(menu);
            row.addView(delete);
            menuButtons.add(menu);
            deleteButtons.add(delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    remove_menu((String) menu.getTag());
                    row.removeView(delete);
                    row.removeView(menu);
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in;
                    in = new Intent(UserMainScreen.this, Menu_Page.class);
                    in.putExtra("menu_name", (String) menu.getTag());
                    in.putExtra("user_name", user_name);
                    startActivity(in);
                    finish();
                }
            });
        }
    }

    void getMenuNameFromUser(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserMainScreen.this);
        alertDialog.setMessage("enter menu name");
        final EditText editMenu = new EditText(UserMainScreen.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editMenu.setLayoutParams(lp);
        alertDialog.setView(editMenu);
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String menu_name = editMenu.getText().toString().toLowerCase(Locale.ROOT);
                if (user_menus.containsKey(menu_name)){
                    dialogInterface.dismiss();
                }
                else {
                    removeExistingMenus();
                    addNewMenu(menu_name);
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

    void addName(){
        hello = findViewById(R.id.Hello);
        String hello_name = "Hello "+ user_name;
        hello.setText(hello_name);
    }


    void AdminRequest(String admin_mail){
        String user_mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference user = db.collection("users").document(user_mail);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserMainScreen.this);
        alertDialog.setMessage("Admin: " + admin_mail + " Wants to add you to their users.");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DocumentReference admin = db.collection("users").document(admin_mail);
                admin.update("users", FieldValue.arrayUnion(user));
                user.update("admin_ref", admin);
                user.update("message", FieldValue.delete());
                dialogInterface.dismiss();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.update("message", FieldValue.delete());
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }
    void getUserName(){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference docRef = db.collection("users").document(mail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                        user_name = (String) document.getData().get("name");
                        String admin_request = (String) document.getData().get("message");
                        if (admin_request != null){
                            AdminRequest(admin_request);
                        }
                        getUserMenus();

                    } else {
                        Log.d("main_activity", "No such document");
                    }
                } else {
                    Log.d("main_activity", "get failed with ", task.getException());
                }
            }
        });
    }

    void getUserMenus(){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("users/" + mail + "/menus")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("main_activity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                user_menus.put(document.getId(), document.getData());
                            }
                            mainFunction();
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void remove_menu(String menu_name){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("users/" + mail + "/menus").document(menu_name)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("main_activity", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("main_activity", "Error deleting document", e);
                    }
                });
    }

    void addNewMenu(String menu_name){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Map<String, Object> menu = new HashMap<>();
        menu.put("total_cals", 0);
        db.collection("users/" + mail + "/menus").document(menu_name)
                .set(menu)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("main_activity", "user successfully written to DB!");
                        getUserMenus();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("main_activity", "Error writing user document", e);
                    }
                });
    }

    public void Logout() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
}