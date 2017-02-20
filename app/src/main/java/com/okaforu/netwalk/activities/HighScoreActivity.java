package com.okaforu.netwalk.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.okaforu.netwalk.R;
import com.okaforu.netwalk.adapters.ScoreAdapter;
import com.okaforu.netwalk.models.Difficulty;
import com.okaforu.netwalk.models.Score;
import com.okaforu.netwalk.sql.ScoreSqlHelper;
import com.okaforu.netwalk.utils.SettingsPreference;

import java.util.List;

public class HighScoreActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private ScoreSqlHelper sqlHelper;
    private ScoreAdapter scoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);

        initializeComponents();
        initializeUI();
        initializeUIEvents();
    }

    private void initializeUI() {
        RecyclerView rvHighScores = (RecyclerView) findViewById(R.id.rvHighScores);
        rvHighScores.setHasFixedSize(true);
        rvHighScores.setItemAnimator(new DefaultItemAnimator());
        rvHighScores.setLayoutManager(new LinearLayoutManager(this));
        rvHighScores.setAdapter(scoreAdapter);
    }

    private void initializeComponents() {
        sqlHelper = new ScoreSqlHelper(getApplicationContext());
        scoreAdapter = new ScoreAdapter();
    }

    private void initializeUIEvents() {
        RadioButton rbEasy = (RadioButton) findViewById(R.id.rbEasy);
        RadioButton rbNormal = (RadioButton) findViewById(R.id.rbNormal);
        RadioButton rbHard = (RadioButton) findViewById(R.id.rbHard);

        rbEasy.setOnCheckedChangeListener(this);
        rbNormal.setOnCheckedChangeListener(this);
        rbHard.setOnCheckedChangeListener(this);

        SettingsPreference settings = SettingsPreference.getInstance(this);
        if (settings != null && settings.isSettingExists()) {
            Difficulty difficulty = settings.getSavedDifficulty();

            if (difficulty == Difficulty.EASY) {
                rbEasy.setChecked(true);
            } else if (difficulty == Difficulty.NORMAL) {
                rbNormal.setChecked(true);
            } else if (difficulty == Difficulty.HARD) {
                rbHard.setChecked(true);
            }
        }
    }

    /**
     * Get's a list of high scores based on difficulty level that the user selects
     * @param compoundButton
     * @param b
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.isChecked()) {
            String text = compoundButton.getText().toString();
            Difficulty selectedDifficulty = Difficulty.valueOf(text.toUpperCase());
            List<Score> scores = sqlHelper.getAllHighScores(selectedDifficulty);
            scoreAdapter.replaceScores(scores);
        }
    }
}