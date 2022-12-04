package com.example.calories_calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
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

    EditText usernameEditText, emailEditText, passwordEditText,
            birthdayEditText, heightEditText, weightEditText;
    Button register_button, back;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        back = findViewById(R.id.Return_button);
        back.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        back.setOnClickListener(v -> main_screen());

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
        int height = 0;
        int weight = 0;
        if (!heightEditText.getText().toString().equals("")){
            height = Integer.parseInt(heightEditText.getText().toString());
        }
        if (!weightEditText.getText().toString().equals("")){
            weight = Integer.parseInt(weightEditText.getText().toString());
        }

        boolean isValid = validateData(username, email, password, height, weight);
        if(!isValid){ return;}

        createAccountInFirebase(username, email, password, height, weight);


    }
      boolean validateData(String username, String email, String password, int height, int weight){
        // validate the data we got from the user.
          boolean valid_data = true;
          if(username.isEmpty()){
              usernameEditText.setError("UserName is invalid");
              valid_data = false;
          }

          if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
              emailEditText.setError("Email is invalid");
              valid_data = false;
          }
          if(password.length()<6){
              passwordEditText.setError("Password length is invalid");
              valid_data = false;
          }
          if (height <= 0){
              heightEditText.setError("invalid height");
              valid_data = false;
          }
          if (weight <= 0){
              weightEditText.setError("invalid weight");
              valid_data = false;
          }
          return valid_data;
    }

    void createAccountInFirebase(String username, String email, String password, int height, int weight){
        changeInProgress(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);
                if(task.isSuccessful()){ //creating account is done
                  firebaseAuth.signOut();
                  createAccountInFirebaseDB(username, email, height, weight);
                  Toast.makeText(Register.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                  main_screen();
              } else{ // failure while creating the account
                  Toast.makeText(Register.this,task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void createAccountInFirebaseDB(String username, String email, int height, int weight){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("name", username);
        user.put("height", height);
        user.put("weight", weight);
        user.put("is_admin", false);
        user.put("admin_mail", null);
        user.put("menus", new HashMap<>());
        db.collection("users").document(email)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("main_activity", "user successfully written to DB!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("main_activity", "Error writing user document", e);
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
        finish();
    }
}