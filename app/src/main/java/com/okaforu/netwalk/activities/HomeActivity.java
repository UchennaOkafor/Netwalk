package com.okaforu.netwalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.okaforu.netwalk.R;
import com.okaforu.netwalk.fragments.SettingsFragment;
import com.okaforu.netwalk.utils.SettingsPreference;

public class HomeActivity extends AppCompatActivity {

    private SettingsPreference settings;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        settings = SettingsPreference.getInstance(this);
        settingsFragment = SettingsFragment.newInstance(this, GameActivity.class);
        initializeEvents();
    }

    private void initializeEvents() {
        findViewById(R.id.btnPlayGame).setOnClickListener(new PlayGameButtonHandler());
        findViewById(R.id.btnViewHighscores).setOnClickListener(new ViewHighScoresButtonHandler());
    }

    private class PlayGameButtonHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (! settings.isSettingExists()) {
                settingsFragment.show(getSupportFragmentManager(), SettingsFragment.TAG);
            } else {
                startActivity(new Intent(getApplicationContext(), GameActivity.class));
            }
        }
    }

    private class ViewHighScoresButtonHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(getApplicationContext(), HighScoreActivity.class));
        }
    }
}