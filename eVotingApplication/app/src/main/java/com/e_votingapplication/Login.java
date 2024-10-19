package com.e_votingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextInputEditText userEmail;
    private TextInputEditText userPassword;
    private Button loginBtn;
    TextView signUpLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();

        userEmail = findViewById(R.id.user_email);
        userPassword = findViewById(R.id.user_password);
        loginBtn = findViewById(R.id.login_btn);
        signUpLink = findViewById(R.id.textView_signUpLink);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the Sign-Up activity
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });


    }

    private void loginUser() {
        String email = Objects.requireNonNull(userEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(userPassword.getText()).toString().trim();

        if (TextUtils.isEmpty(email)) {
            userEmail.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            userPassword.setError("Password is required.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        //
                        Intent intent = new Intent(Login.this, UserPanel.class);
                        intent.putExtra("userEmail", email);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user
                        Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
}