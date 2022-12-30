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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserMainScreen extends AppCompatActivity {
    FirestoreWrapper wrapper = new FirestoreWrapper();
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addNewMenu;
    TextView hello;
    TableLayout table;
    String userName;
    boolean isAdmin;
    String userMail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    ArrayList<Button> menuButtons = new ArrayList<>();
    ArrayList<ImageButton> deleteButtons = new ArrayList<>();
    Map<String, Object> userMenus = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_screen);
        addNewMenu = findViewById(R.id.addNewMenu);
        isAdmin = (boolean) getIntent().getExtras().get("isAdmin");
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        addNewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMenuNameFromUser();
            }
        });
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Intent in;
                switch (item.getItemId()) {
                    case R.id.user_home:
                        break;
                    case R.id.user_search:
                        in = new Intent(getApplicationContext(), UserSearch.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.user_suggestions:
                        in = new Intent(getApplicationContext(), UserSuggestions.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.admin_users:
                        in = new Intent(getApplicationContext(), AdminMainScreen.class);
                        in.putExtra("isAdmin", isAdmin);
                        startActivity(in);
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
        if (isAdmin) {
            bottomNavigationView.getMenu().removeItem(R.id.user_suggestions);
        } else {
            bottomNavigationView.getMenu().removeItem(R.id.admin_users);
        }
        getUserName();
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
        userMenus.clear();
    }

    void addMenus(){
        if (userMenus.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.Table);
        for (Map.Entry<String,Object> entry : userMenus.entrySet()){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
            Long totalCals = (Long) ((Map<String,Object>) entry.getValue()).get("totalCals");
            String menuText = entry.getKey() + " (total calories: " + totalCals + ")";
            menu.setText(menuText);
            menu.setGravity(Gravity.CENTER);
            menu.setTextSize(12);
            menu.setHeight(20);
            menu.setWidth(900);
            menu.setPadding(0,20, 10,20);

            ImageButton delete= new ImageButton(this);
            delete.setImageResource(R.drawable.ic_menu_delete);
            row.addView(menu);
            row.addView(delete);
            menuButtons.add(menu);
            deleteButtons.add(delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeMenu((String) menu.getTag());
                    row.removeView(delete);
                    row.removeView(menu);
                }
            });
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in;
                    in = new Intent(UserMainScreen.this, MenuPage.class);
                    in.putExtra("menuName", (String) menu.getTag());
                    in.putExtra("userName", userName);
                    in.putExtra("isAdmin", isAdmin);
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
                String menuName = editMenu.getText().toString().toLowerCase(Locale.ROOT);
                if (userMenus.containsKey(menuName)){
                    dialogInterface.dismiss();
                }
                else {
                    removeExistingMenus();
                    addNewMenu(menuName);
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
        String helloName = "Hello "+ userName;
        hello.setText(helloName);
    }


    void AdminRequest(String adminMail){
        DocumentReference user = wrapper.getDocumentRef("users/"+userMail);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserMainScreen.this);
        alertDialog.setMessage("Admin: " + adminMail + " Wants to add you to their users.");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DocumentReference admin = wrapper.getDocumentRef("users/"+adminMail);
                admin.update("users", FieldValue.arrayUnion(user));
                user.update("adminRef", admin);
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
        wrapper.getDocument("users/"+userMail)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userName = (String) document.getData().get("name");
                        String adminRequest = (String) document.getData().get("message");
                        if (adminRequest != null){
                            AdminRequest(adminRequest);
                        }
                        getUserMenus();
                    }
                }
            }
        });
    }

    void getUserMenus(){
        wrapper.getCollectionRef("users/"+userMail+"/menus")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("mainActivity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userMenus.put(document.getId(), document.getData());
                            }
                            mainFunction();
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void removeMenu(String menuName){
        wrapper.getCollectionRef("users/" + userMail + "/menus/" + menuName + "/meals")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("mainActivity", "success get meals to delete");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().delete();
                            }
                        } else {
                            Log.d("mainActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
        wrapper.getDocumentRef("users/" + userMail + "/menus/"+menuName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("mainActivity", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("mainActivity", "Error deleting document", e);
                    }
                });
    }

    void addNewMenu(String menuName){
        Map<String, Object> menu = new HashMap<>();
        menu.put("totalCals", 0);
        wrapper.setDocument("users/" + userMail + "/menus/"+menuName,menu)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getUserMenus();
                    }
                });
    }
}