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
import com.google.firebase.firestore.FieldValue;
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
            Button user = new Button(this);
            user.setTag(adminUsers.get(i));
            user.setText(adminUsers.get(i));
            user.setGravity(Gravity.CENTER);
            user.setTextSize(15);
            user.setHeight(30);
            user.setWidth(900);
            ImageButton delete = new ImageButton(this);
            delete.setImageResource(R.drawable.ic_menu_delete);
            row.addView(user);
            row.addView(delete);
            usersButtons.add(user);
            deleteButtons.add(delete);
            user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent in;
                    in = new Intent(AdminMainScreen.this, AdminUserMenus.class);
                    System.out.println((String) user.getTag());
                    System.out.println(adminName);
                    in.putExtra("userMail", (String) user.getTag());
                    in.putExtra("adminName", adminName);
                    startActivity(in);
                    finish();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeUser((String) user.getTag());
                    row.removeView(delete);
                    row.removeView(user);
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
                String menuName = editMenu.getText().toString();
                if (adminUsers.contains(menuName)) {
                    Toast.makeText(AdminMainScreen.this, "You are already the admin of this user.", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                } else {
                    removeExistingUsers();
                    addNewUser(menuName);
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
        String helloName = "Hello " + adminName;
        welcome.setText(helloName);
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
                        Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                        adminName = (String) document.getData().get("name");
                        ArrayList<DocumentReference> userList = (ArrayList<DocumentReference>) document.getData().get("users");
                        if (userList != null){
                            for (int i =0; i< userList.size();i++){
                                adminUsers.add(userList.get(i).getId());
                            }
                        }
                        mainFunction();
                    } else {
                        Log.d("mainActivity", "No such document");
                    }
                } else {
                    Log.d("mainActivity", "get failed with ", task.getException());
                }
            }
        });
    }
    void addNewUser(String userMail){
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference docRef = db.collection("users").document(userMail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                        boolean isAdmin = (boolean) document.getData().get("isAdmin");
                        if (adminUsers.contains(userMail)){
                            Toast.makeText(AdminMainScreen.this, "You are already the admin of this user.", Toast.LENGTH_SHORT).show();
                        }
                        if (isAdmin == false){
                            docRef.update("message", mail);
                        }
                        else{
                            Toast.makeText(AdminMainScreen.this, "This user is admin", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdminMainScreen.this, "This user doesn't exist", Toast.LENGTH_SHORT).show();
                        Log.d("mainActivity", "No such document");
                    }
                } else {
                    Log.d("mainActivity", "get failed with ", task.getException());
                }
            }
        });
    }

    void removeUser(String userMail) {
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference admin = db.collection("users").document(mail);
        DocumentReference user = db.collection("users").document(userMail);
        admin.update("users", FieldValue.arrayRemove(user));
    }

    public void Logout() {
        Intent intent = new Intent(this, Login.class);
        FirebaseAuth.getInstance().signOut();
        startActivity(intent);
        finish();
    }
}
