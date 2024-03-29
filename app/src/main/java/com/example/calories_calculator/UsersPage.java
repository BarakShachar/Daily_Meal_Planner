package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class UsersPage extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FloatingActionButton addNewUsers;
    TableLayout table;
    String adminName;
    ArrayList<Button> usersButtons = new ArrayList<>();
    ArrayList<ImageButton> deleteButtons = new ArrayList<>();
    ArrayList<String> adminUsers = new ArrayList<>();
    boolean isAdmin;
    FirestoreWrapper.AdminMainScreenWrapper wrapper = new FirestoreWrapper.AdminMainScreenWrapper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_page);
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
        wrapper.getAdminData();
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
                    wrapper.removeUser((String) user.getTag());
                    row.removeView(delete);
                    row.removeView(user);
                }
            });
        }
    }

    void getUsersFromAdmin() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UsersPage.this);
        alertDialog.setMessage("enter user name");
        final EditText editMenu = new EditText(UsersPage.this);
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
                    Toast.makeText(UsersPage.this, "You are already the admin of this user.", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                } else {
                    removeExistingUsers();
                    wrapper.addNewUser(menuName);
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
