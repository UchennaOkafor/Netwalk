package com.okaforu.netwalk.models;

public class Score {

    private int rank;
    private int moves;
    private String username;
    private long secondsTaken;
    private Difficulty difficulty;

    public Score(int rank, int moves, String username, long secondsTaken, Difficulty difficulty) {
        this.rank = rank;
        this.moves = moves;
        this.username = username;
        this.secondsTaken = secondsTaken;
        this.difficulty = difficulty;
    }

    public Score(int moves, String username, long timeTaken, Difficulty difficulty) {
        this(0, moves, username, timeTaken, difficulty);
    }

    public int getRank() {
        return rank;
    }

    public int getMoves() {
        return moves;
    }

    public String getUsername() {
        return username;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public long getSecondsTaken() {
        return secondsTaken;
    }
}