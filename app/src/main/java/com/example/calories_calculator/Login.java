package com.example.calories_calculator;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

public class Login extends AppCompatActivity {
    TextView emailEditText, passwordEditText, signInBtnEditText;
    Button login_button;
    ProgressBar loginProgressBar;
    FirebaseFirestore database;
    Create_user user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEditText = findViewById(R.id.Username);
        passwordEditText = findViewById(R.id.Password);
        login_button = findViewById(R.id.Login_button);
        signInBtnEditText = findViewById(R.id.Sign_in);
        loginProgressBar = findViewById(R.id.progressBarLogin);

        login_button.setOnClickListener((v) -> loginUser());
        signInBtnEditText.setOnClickListener((v) -> startActivity(new Intent(Login.this, Register.class)));
    }

    void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        boolean isValid = validateData(email, password);
        if (!isValid) {
            return;
        }
        logInUserInFirebase(email, password);

    }

    void logInUserInFirebase(String email, String password) {
        changeInProgress(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);
                if (task.isSuccessful()) { //login is successful
                    Toast.makeText(Login.this, "login successfully!", Toast.LENGTH_SHORT).show();
                    // this is where we need to get the user value from the database by his email address
                    // after we got his value, we can check if he's a user or admin
                    Create_user user = new Create_user();
                    connect(user);
                } else {
                    Toast.makeText(Login.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void changeInProgress(boolean inProgress) {
        if (inProgress) {
            loginProgressBar.setVisibility(View.VISIBLE);
            login_button.setVisibility(View.GONE);
        } else {
            loginProgressBar.setVisibility(View.GONE);
            login_button.setVisibility(View.VISIBLE);
        }
    }

    boolean validateData(String email, String password) {
        // validate the data we got from the user.

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email is invalid");
            return false;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password length is invalid");
            return false;
        }
        return true;
    }

    public void connect(Create_user user) {
        //we need to add a check if the user is admin or not. after that we will send him to the right screen//
        Intent in;
        if (user.isAdmin) {
            // if isAdmin is true - it means that we need to send him to the admin's pages.
            in = new Intent(Login.this, AdminMainScreen.class);
        } else {  // if isAdmin is false - it means that we need to send him to the user's pages.
            in = new Intent(Login.this, UserMainScreen.class);
        }
        startActivity(in);
    }
}


