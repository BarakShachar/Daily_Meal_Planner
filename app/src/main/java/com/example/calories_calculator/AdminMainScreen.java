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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminMainScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addNewUsers;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView welcome;
    TableLayout table;
    String adminName;
    Button logout;
    ArrayList<Button> usersButtons = new ArrayList<>();
    ArrayList<ImageButton> deleteButtons = new ArrayList<>();
    ArrayList<String> adminUsers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main_screen);
        addNewUsers = findViewById(R.id.addNewUser);
        addNewUsers.setOnClickListener(v -> getUsersFromAdmin());
        logout = findViewById(R.id.logOut);
        logout.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        logout.setOnClickListener(v -> Logout());


        bottomNavigationView = findViewById(R.id.AdminBottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.admin_users:
                        startActivity(new Intent(getApplicationContext(), AdminMainScreen.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.admin_edit:
                        startActivity(new Intent(getApplicationContext(), AdminEdit.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
        getAdminData();
    }

    void mainFunction() {
        addName();
        addUsers();
    }

    void removeExistingUsers() {
        for (int i = 0; i < usersButtons.size(); i++) {
            ViewGroup layout = (ViewGroup) usersButtons.get(i).getParent();
            if (null != layout) //for safety only  as you are doing onClick
                layout.removeView(usersButtons.get(i));
        }
        for (int i = 0; i < deleteButtons.size(); i++) {
            ViewGroup layout = (ViewGroup) deleteButtons.get(i).getParent();
            if (null != layout) //for safety only  as you are doing onClick
                layout.removeView(deleteButtons.get(i));
        }
        usersButtons.clear();
        deleteButtons.clear();
        adminUsers.clear();
    }

    void addUsers() {
        if (adminUsers.isEmpty()) {
            return;
        }
        table = (TableLayout) findViewById(R.id.Table);
        for (int i=0; i<adminUsers.size();i++){
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(adminUsers.get(i));
            menu.setText(adminUsers.get(i));
            menu.setGravity(Gravity.CENTER);
            menu.setTextSize(15);
            menu.setHeight(30);
            menu.setWidth(900);
            ImageButton delete = new ImageButton(this);
            delete.setImageResource(R.drawable.ic_menu_delete);
            row.addView(menu);
            row.addView(delete);
            usersButtons.add(menu);
            deleteButtons.add(delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeUser((String) menu.getTag());
                    row.removeView(delete);
                    row.removeView(menu);
                }
            });
        }
    }

    void getUsersFromAdmin() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdminMainScreen.this);
        alertDialog.setMessage("enter user name");
        final EditText editMenu = new EditText(AdminMainScreen.this);
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
                if (adminUsers.contains(menu_name)) {
                    Toast.makeText(AdminMainScreen.this, "You are already the admin of this user.", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                } else {
                    removeExistingUsers();
                    addNewUser(menu_name);
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


    void addName() {
        welcome = findViewById(R.id.Hello);
        String hello_name = "Hello " + adminName;
        welcome.setText(hello_name);
    }

    void getAdminData() {
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference docRef = db.collection("users").document(mail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                        adminName = (String) document.getData().get("name");
                        ArrayList<DocumentReference> userList = (ArrayList<DocumentReference>) document.getData().get("users");
                        if (userList != null){
                            for (int i =0; i< userList.size();i++){
                                adminUsers.add(userList.get(i).getId());
                            }
                        }
                        mainFunction();
                    } else {
                        Log.d("main_activity", "No such document");
                    }
                } else {
                    Log.d("main_activity", "get failed with ", task.getException());
                }
            }
        });
    }
    void addNewUser(String user_mail){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference docRef = db.collection("users").document(user_mail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("main_activity", "DocumentSnapshot data: " + document.getData());
                        boolean is_admin = (boolean) document.getData().get("is_admin");
                        if (adminUsers.contains(user_mail)){
                            Toast.makeText(AdminMainScreen.this, "You are already the admin of this user.", Toast.LENGTH_SHORT).show();
                        }
                        if (is_admin == false){
                            docRef.update("message", mail);
                        }
                        else{
                            Toast.makeText(AdminMainScreen.this, "This user is admin", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdminMainScreen.this, "This user doesn't exist", Toast.LENGTH_SHORT).show();
                        Log.d("main_activity", "No such document");
                    }
                } else {
                    Log.d("main_activity", "get failed with ", task.getException());
                }
            }
        });
    }

    void removeUser(String menu_name) {
        //TODO: fix that
    }

    public void Logout() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
}
