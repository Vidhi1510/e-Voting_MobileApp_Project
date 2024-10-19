package com.e_votingapplication.model;

import java.util.List;

public class Poll {
    private String question;
    private List<String> options;

    public Poll() {
        // Default constructor required for calls to DataSnapshot.getValue(Poll.class)
    }

    public Poll(String question, List<String> options) {
        this.question = question;
        this.options = options;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }
}
