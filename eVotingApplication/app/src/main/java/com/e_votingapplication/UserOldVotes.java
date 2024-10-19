package com.e_votingapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.e_votingapplication.model.Poll;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Objects;

public class UserOldVotes extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout oldVotesLayout;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_old_votes);

        oldVotesLayout = findViewById(R.id.votedPollsLayout);
        db = FirebaseFirestore.getInstance();

        // Get the current user's ID
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Fetch and display the user's old votes
        fetchAndDisplayUserVotes();
    }

    // Function to fetch and display the user's old votes
    private void fetchAndDisplayUserVotes() {
        // Query all polls
        db.collection("polls").get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot pollDocument : querySnapshot.getDocuments()) {
                String pollId = pollDocument.getId();

                // Query the votes sub-collection for the current user
                db.collection("polls").document(pollId)
                        .collection("votes")
                        .document(userId)
                        .get()
                        .addOnSuccessListener(voteSnapshot -> {
                            if (voteSnapshot.exists()) {
                                // User has voted in this poll
                                Long selectedOptionId = voteSnapshot.getLong("selectedOptionId");
                                Poll poll = pollDocument.toObject(Poll.class);

                                // Display the poll question and the user's selected option
                                if (poll != null && selectedOptionId != null && selectedOptionId >= 0 && selectedOptionId < poll.getOptions().size()) {
                                    displayOldVote(poll, selectedOptionId.intValue());
                                }
                            }
                        }).addOnFailureListener(e -> {
                            System.err.println("Error fetching user vote: " + e.getMessage());
                        });
            }
        }).addOnFailureListener(e -> {
            System.err.println("Error fetching polls: " + e.getMessage());
        });
    }

    // Function to display the user's old vote for a specific poll
    private void displayOldVote(Poll poll, int selectedOptionId) {
        // Create a TextView for the poll question
        TextView questionTextView = new TextView(this);
        questionTextView.setText("Poll Question: " + poll.getQuestion());
        questionTextView.setTextSize(16f);
        questionTextView.setPadding(0, 16, 0, 8);

        // Create a TextView for the user's selected option
        TextView selectedOptionTextView = new TextView(this);
        selectedOptionTextView.setText("Your Selected Option: " + poll.getOptions().get(selectedOptionId));
        selectedOptionTextView.setTextSize(14f);
        selectedOptionTextView.setPadding(0, 8, 0, 16);

        // Add the TextViews to the layout
        oldVotesLayout.addView(questionTextView);
        oldVotesLayout.addView(selectedOptionTextView);
    }
}
