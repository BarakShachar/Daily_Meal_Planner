package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

public class AdminMainScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addNewUsers;
    TableLayout table;
    String adminName;
    ArrayList<Button> usersButtons = new ArrayList<>();
    ArrayList<ImageButton> deleteButtons = new ArrayList<>();
    ArrayList<String> adminUsers = new ArrayList<>();
    String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    boolean isAdmin;
    FirestoreWrapper wrapper = new FirestoreWrapper();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main_screen);
        addNewUsers = findViewById(R.id.addNewUser);
        addNewUsers.setOnClickListener(v -> getUsersFromAdmin());
        bottomNavigationView = findViewById(R.id.BottomNavigation);
        isAdmin = (boolean) getIntent().getExtras().get("isAdmin");
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
        getAdminData();
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


    void getAdminData() {
        wrapper.getDocument("users/" + mail)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        adminName = (String) document.getData().get("name");
                        ArrayList<DocumentReference> userList = (ArrayList<DocumentReference>) document.getData().get("users");
                        if (userList != null){
                            for (int i =0; i< userList.size();i++){
                                adminUsers.add(userList.get(i).getId());
                            }
                        }
                        addUsers();
                    }
                }
            }
        });
    }
    void addNewUser(String userMail){
        DocumentReference docRef = wrapper.getDocumentRef("users/" + userMail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        boolean isAdmin = (boolean) document.getData().get("isAdmin");
                        if (adminUsers.contains(userMail)){
                            Toast.makeText(AdminMainScreen.this, "You are already the admin of this user.", Toast.LENGTH_SHORT).show();
                        }
                        if (!isAdmin){
                            docRef.update("message", mail);
                        }
                        else{
                            Toast.makeText(AdminMainScreen.this, "This user is admin", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdminMainScreen.this, "This user doesn't exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    void removeUser(String userMail) {
        DocumentReference admin = wrapper.getDocumentRef("users/" + mail);
        DocumentReference user = wrapper.getDocumentRef("users/" + userMail);
        admin.update("users", FieldValue.arrayRemove(user));
        user.update("adminRef", FieldValue.delete());
    }
}
