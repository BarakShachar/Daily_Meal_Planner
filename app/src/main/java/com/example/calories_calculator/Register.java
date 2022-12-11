package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText usernameEditText, emailEditText, passwordEditText,confirmPassword;
    Button registerButton, back;
    ProgressBar progressBar;
    CheckBox admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        back = findViewById(R.id.Return_button);
        back.setOnClickListener(v -> mainScreen());

        usernameEditText = findViewById(R.id.Register_username);
        emailEditText = findViewById(R.id.Register_email);
        passwordEditText = findViewById(R.id.Register_password);
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        confirmPassword = findViewById(R.id.Repeat_password);
        confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        registerButton = (Button) findViewById(R.id.Register_button);
        progressBar = findViewById(R.id.progressBar);
        registerButton.setOnClickListener(v -> createAccount());
        admin = findViewById(R.id.admin);
    }
      void createAccount(){
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirm = confirmPassword.getText().toString();
        boolean isAdmin =admin.isChecked();

          boolean isValid = validateData(username, email, password, confirm);
        if(!isValid){ return;}

        createAccountInFirebase(username, email, password, isAdmin);


    }
      boolean validateData(String username, String email, String password,String confirm){
        // validate the data we got from the user.
          boolean validData = true;
          if(username.isEmpty()){
              usernameEditText.setError("UserName is invalid");
              validData = false;
          }

          if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
              emailEditText.setError("Email is invalid");
              validData = false;
          }
          if(password.length()<6){
              passwordEditText.setError("Password length is invalid");
              validData = false;
          }
          if (confirm.length() <6){
              confirmPassword.setError("Password length is invalid");
              validData = false;
          }
          if (!password.equals(confirm)){
              confirmPassword.setError("the password isn't equal");
              validData = false;
          }
          return validData;
    }

    void createAccountInFirebase(String username, String email, String password, boolean isAdmin){
        changeInProgress(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);
                if(task.isSuccessful()){ //creating account is done
                  createAccountInFirebaseDB(username, email, isAdmin);
                  Toast.makeText(Register.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                  mainScreen();
              } else{ // failure while creating the account
                  Toast.makeText(Register.this,task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void createAccountInFirebaseDB(String username, String email, boolean isAdmin){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("name", username);
        user.put("isAdmin", isAdmin);
        user.put("adminMail", null);
        db.collection("users").document(email)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("mainActivity", "user successfully written to DB!");
                        signOut();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("mainActivity", "Error writing user document", e);
                        signOut();
                    }
                });
    }

    void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
        }
    }

    public void mainScreen() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
}