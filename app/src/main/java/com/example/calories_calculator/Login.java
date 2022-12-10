package com.example.calories_calculator;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    TextView emailEditText, passwordEditText, signUpBtnEditText, forgotPassword;
    Button loginButton;
    ProgressBar loginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEditText = findViewById(R.id.Username);
        passwordEditText = findViewById(R.id.Password);
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        loginButton = findViewById(R.id.Login_button);
        signUpBtnEditText = findViewById(R.id.Sign_up);
        loginProgressBar = findViewById(R.id.progressBarLogin);
        loginButton.setOnClickListener((v) -> loginUser());
        forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener((v)-> resendPassword());
        signUpBtnEditText.setOnClickListener((v) -> signUpAction());
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

    void resendPassword(){
        String email = emailEditText.getText().toString();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Login.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void signUpAction(){
        startActivity(new Intent(Login.this, Register.class));
        finish();
    }

    void logInUserInFirebase(String email, String password) {
        changeInProgress(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) { //login is successful
                    Toast.makeText(Login.this, "login successfully!", Toast.LENGTH_SHORT).show();
                    getUserData(email);
                } else {
                    changeInProgress(false);
                    Toast.makeText(Login.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void changeInProgress(boolean inProgress) {
        if (inProgress) {
            loginProgressBar.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
        } else {
            loginProgressBar.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }
    }

    void getUserData(String mail){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(mail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("mainActivity", "DocumentSnapshot data: " + document.getData());
                        connect((Boolean) document.getData().get("isAdmin"));
                    } else {
                        Log.d("mainActivity", "No such document");
                    }
                } else {
                    Log.d("mainActivity", "get failed with ", task.getException());
                }
            }
        });
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

    public void connect(Boolean isAdmin) {
        //we need to add a check if the user is admin or not. after that we will send him to the right screen//
        Intent in;
        in = new Intent(Login.this, UserMainScreen.class);
        in.putExtra("isAdmin", isAdmin);
        changeInProgress(false);
        startActivity(in);
        finish();
    }
}


