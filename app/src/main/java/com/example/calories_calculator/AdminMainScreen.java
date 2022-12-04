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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminMainScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addNewUsers;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView welcome;
    TableLayout table;
    String user_name;
    Button logout;
    ArrayList<Button> usersButtons = new ArrayList<>();
    ArrayList<ImageButton> deleteButtons = new ArrayList<>();
    Map<String, Object> adminUsers = new HashMap<>();


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
        getUserName();
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
        for (Map.Entry<String, Object> entry : adminUsers.entrySet()) {
            TableRow row = new TableRow(this);
            table.addView(row);
            Button menu = new Button(this);
            menu.setTag(entry.getKey());
            Long total_cals = (Long) ((Map<String, Object>) entry.getValue()).get("total cals");
            String menu_text = entry.getKey() + " (total calories: " + total_cals + ")";
            menu.setText(menu_text);
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
                if (adminUsers.containsKey(menu_name)) {
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
        String hello_name = "Hello " + user_name;
        welcome.setText(hello_name);
    }

    void getUserName() {
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
                        getAdminUsers();
                    } else {
                        Log.d("main_activity", "No such document");
                    }
                } else {
                    Log.d("main_activity", "get failed with ", task.getException());
                }
            }
        });
    }

    void getAdminUsers() {
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("users/" + mail + "/users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("main_activity", "success get menus");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                adminUsers.put(document.getId(), document.getData());
                            }
                            mainFunction();
                        } else {
                            Log.d("main_activity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    void removeUser(String menu_name) {
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("users/" + mail + "/users").document(menu_name)
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

    void addNewUser(String menu_name) {
        String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Map<String, Object> user = new HashMap<>();
        db.collection("users/" + mail + "/users").document(menu_name)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("main_activity", "user successfully written to DB!");
                        getAdminUsers();
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
