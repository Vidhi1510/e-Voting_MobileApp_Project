package com.e_votingapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.e_votingapplication.model.Poll;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserVotingPanel extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout pollsLayout;
    private Button submitAllButton;
    private Map<String, Integer> selectedOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_voting_panel);


        pollsLayout = findViewById(R.id.pollsLayout);
        submitAllButton = findViewById(R.id.submitAllButton);
        selectedOptions = new HashMap<>();

        db = FirebaseFirestore.getInstance();

        // Fetch all active polls from Firestore
        Query activePollsQuery = db.collection("polls").whereEqualTo("active", true);
        activePollsQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String documentId = documentSnapshot.getId();
                Poll poll = documentSnapshot.toObject(Poll.class);

                // Add the poll question and options to the layout
                addPollToLayout(documentId, poll);
            }
        }).addOnFailureListener(e -> {
            // Handle error fetching polls
        });

        // Set up OnClickListener for the submit button
        submitAllButton.setOnClickListener(v -> submitAllResponses());
    }

    // Function to add a poll to the layout
    private void addPollToLayout(String documentId, Poll poll) {
        // Create a TextView for the poll question
        TextView questionTextView = new TextView(this);
        questionTextView.setText(poll.getQuestion());
        questionTextView.setTextSize(16f);
        questionTextView.setPadding(0, 16, 0, 8);

        // Add the question TextView to the polls layout
        pollsLayout.addView(questionTextView);

        // Create a RadioGroup for the options
        RadioGroup optionsRadioGroup = new RadioGroup(this);
        optionsRadioGroup.setOrientation(RadioGroup.VERTICAL);

        // Add the options as RadioButtons to the RadioGroup
        List<String> options = poll.getOptions();
        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(options.get(i));
            radioButton.setId(i);
            optionsRadioGroup.addView(radioButton);
        }

        // Add the RadioGroup to the polls layout
        pollsLayout.addView(optionsRadioGroup);

        // Store the selected option for this poll in the map
        optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedOptions.put(documentId, checkedId);
        });
    }

    // Function to submit all responses
    private void submitAllResponses() {
        // Identify the user (assuming you are using Firebase Authentication)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Iterate through the selected options for each poll
        for (Map.Entry<String, Integer> entry : selectedOptions.entrySet()) {
            String documentId = entry.getKey();
            Integer selectedOptionId = entry.getValue();

            // Get the DocumentReference for the poll
            DocumentReference pollRef = db.collection("polls").document(documentId);

            // Check if the user has already voted in the poll
            DocumentReference voteRef = pollRef.collection("votes").document(userId);
            voteRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // User has already voted
                    System.out.println("User has already voted in this poll.");
                    // You may want to notify the user here
                } else {
                    // User has not voted yet
                    // Fetch the poll document
                    pollRef.get().addOnSuccessListener(pollDocument -> {
                        if (pollDocument.exists()) {
                            Poll poll = pollDocument.toObject(Poll.class);

                            if (poll != null && selectedOptionId >= 0 && selectedOptionId < poll.getOptions().size()) {
                                // Get the option key
                                String optionKey = "option" + selectedOptionId + "Votes";
                                Long currentVotes = pollDocument.getLong(optionKey);
                                if (currentVotes == null) {
                                    currentVotes = 0L;
                                }

                                // Increment the vote count
                                currentVotes++;

                                // Update the poll document with the new vote count
                                Map<String, Object> updates = new HashMap<>();
                                updates.put(optionKey, currentVotes);

                                pollRef.update(updates).addOnSuccessListener(aVoid -> {
                                    // Store the user's vote in the votes sub-collection
                                    Map<String, Object> userVote = new HashMap<>();
                                    userVote.put("selectedOptionId", selectedOptionId);

                                    // Store the user's vote
                                    voteRef.set(userVote).addOnSuccessListener(aVoid1 -> {
                                        System.out.println("User vote stored successfully.");
                                    }).addOnFailureListener(e -> {
                                        System.err.println("Error storing user vote: " + e.getMessage());
                                    });
                                }).addOnFailureListener(e -> {
                                    System.err.println("Error updating vote count: " + e.getMessage());
                                });
                            }
                        }
                    }).addOnFailureListener(e -> {
                        System.err.println("Error fetching poll document: " + e.getMessage());
                    });
                }
            }).addOnFailureListener(e -> {
                System.err.println("Error checking user vote: " + e.getMessage());
            });
        }
    }





}
