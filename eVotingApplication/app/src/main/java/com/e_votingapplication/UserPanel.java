package com.e_votingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.auth.User;


import java.util.Objects;

public class UserPanel extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ImageView userPhoto;
    private TextView userName;
    private TextView userEmail;
    private TextView userCity;
    private Button votingButton;
    private Button viewOldVoteButton, viewVotingResultButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_panel);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // Find views in layout
        userPhoto = findViewById(R.id.user_photo);
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        userCity = findViewById(R.id.user_city);
        votingButton = findViewById(R.id.Voting_btn);
        viewOldVoteButton = findViewById(R.id.view_old_vote);


        loadUserDetails();

        votingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserPanel.this, UserVotingPanel.class);
                startActivity(intent);
            }
        });

        viewOldVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserPanel.this, UserOldVotes.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserDetails() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Display user details
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String city = documentSnapshot.getString("city");
                        userName.setText(name);
                        userEmail.setText(email);
                        userCity.setText(city);
                    } else {
                        Toast.makeText(this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
