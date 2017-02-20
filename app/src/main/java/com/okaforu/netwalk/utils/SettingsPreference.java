package com.okaforu.netwalk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.okaforu.netwalk.models.Difficulty;

/**
 * A util class used to access persistent settings saved using SharedPreferences
 */
public class SettingsPreference {

    private static final String PREFERENCE_KEY = "settings_key";
    private static final String DIFFICULTY_KEY = "difficulty";

    private static SettingsPreference instance;
    private final SharedPreferences sharedPreferences;

    public static SettingsPreference getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsPreference(context);
        }

        return instance;
    }

    private SettingsPreference(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public void saveDifficulty(Difficulty difficultyLevel) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString(DIFFICULTY_KEY, difficultyLevel.toString());
        prefsEditor.apply();
    }

    public Difficulty getSavedDifficulty() {
        return Difficulty.valueOf(sharedPreferences.getString(DIFFICULTY_KEY, null));
    }

    /**
     * Checks if a settings value has been previously saved
     * @return
     */
    public boolean isSettingExists() {
        return sharedPreferences.contains(DIFFICULTY_KEY);
    }
}