package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    EditText usernameEditText, emailEditText, passwordEditText,
            birthdayEditText, heightEditText, weightEditText;
    Button register_button;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.Register_username);
        emailEditText = findViewById(R.id.Register_email);
        passwordEditText = findViewById(R.id.Register_password);
        birthdayEditText = findViewById(R.id.Register_birthday);
        heightEditText = findViewById(R.id.Register_height);
        weightEditText = findViewById(R.id.Register_weight);
        register_button = (Button) findViewById(R.id.Register_button);
        progressBar = findViewById(R.id.progressBar);
        register_button.setOnClickListener(v -> createAccount());
    }
      void createAccount(){
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        int height = Integer.parseInt(heightEditText.getText().toString());
        int weight = Integer.parseInt(weightEditText.getText().toString());

        boolean isValid = validateData(username,email,password);
        if(!isValid){ return;}

        createAccountInFirebase(email,password);


    }
      boolean validateData(String username, String email, String password){
        // validate the data we got from the user.

          if(username.isEmpty()){
              usernameEditText.setError("UserName is invalid");
              return false;
          }

          if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
              emailEditText.setError("Email is invalid");
              return false;
          }
          if(password.length()<6){
              passwordEditText.setError("Password length is invalid");
              return false;
          }
          return true;
    }

    void createAccountInFirebase(String email, String password){
        changeInProgress(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);
                if(task.isSuccessful()){ //creating account is done
                  Toast.makeText(Register.this, "Account created successfully! Check email to verify", Toast.LENGTH_SHORT).show();
                  firebaseAuth.getCurrentUser().sendEmailVerification();
                  firebaseAuth.signOut();
                  main_screen();
              } else{ // failure while creating the account
                  Toast.makeText(Register.this,task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            register_button.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            register_button.setVisibility(View.VISIBLE);
        }
    }

    public void main_screen() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}