package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class Login extends AppCompatActivity {
    TextView emailEditText, passwordEditText, signInBtnEditText;
    Button login_button;
    ProgressBar loginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEditText = (TextView) findViewById(R.id.Username);
        passwordEditText = (TextView) findViewById(R.id.Password);
        login_button = (Button) findViewById(R.id.Login_button);
        signInBtnEditText = (TextView) findViewById(R.id.Sign_in);
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
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);
                if (task.isSuccessful()) { //login is successful
                    Toast.makeText(Login.this, "login successfully!", Toast.LENGTH_SHORT).show();
                    connect();
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

    public void connect() {
        //we need to add a check if the user is admin or not. after that we will send him to the right screen//
        Intent in = new Intent(Login.this, UserMainScreen.class);
        startActivity(in);
    }
}


