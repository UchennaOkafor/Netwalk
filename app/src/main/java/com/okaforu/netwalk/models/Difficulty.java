package com.okaforu.netwalk.models;

/**
 * Represents a difficulty mode for the game
 */
public enum Difficulty {
    EASY,
    NORMAL,
    HARD;

    @Override
    public String toString() {
        return this.name().toUpperCase();
    }
}