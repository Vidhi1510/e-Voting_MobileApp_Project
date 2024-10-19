package com.e_votingapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUp extends AppCompatActivity {

    private TextInputEditText userNameInput;
    private TextInputEditText userEmailInput;
    private TextInputEditText userCityInput;
    private TextInputEditText userPasswordInput;
    private TextInputEditText userCoPasswordInput;
    private Button signUpButton;
    private TextView logInLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userNameInput = findViewById(R.id.user_name);
        userEmailInput = findViewById(R.id.user_email);
        userCityInput = findViewById(R.id.user_city);
        userPasswordInput = findViewById(R.id.user_password);
        userCoPasswordInput = findViewById(R.id.user_copassword);
        signUpButton = findViewById(R.id.signup_btn);
        logInLink = findViewById(R.id.textView_logInLink);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
        logInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the Sign-Up activity
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        });
    }
    private void signUp() {
        // Retrieve input values
        String userName = Objects.requireNonNull(userNameInput.getText()).toString().trim();
        String userEmail = Objects.requireNonNull(userEmailInput.getText()).toString().trim();
        String userCity = Objects.requireNonNull(userCityInput.getText()).toString().trim();
        String userPassword = Objects.requireNonNull(userPasswordInput.getText()).toString().trim();
        String userCoPassword = Objects.requireNonNull(userCoPasswordInput.getText()).toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userEmail) ||
                TextUtils.isEmpty(userCity) || TextUtils.isEmpty(userPassword) ||
                TextUtils.isEmpty(userCoPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!userPassword.equals(userCoPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", userName);
                            userData.put("email", userEmail);
                            userData.put("city", userCity);

                            firestore.collection("users").document(currentUser.getUid())
                                    .set(userData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignUp.this, UserPanel.class);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(this, "Failed to store user data in Firestore", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
