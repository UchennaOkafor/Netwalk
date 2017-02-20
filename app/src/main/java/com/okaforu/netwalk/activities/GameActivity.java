package com.okaforu.netwalk.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.okaforu.netwalk.R;
import com.okaforu.netwalk.fragments.GamePausedFragment;
import com.okaforu.netwalk.fragments.SettingsFragment;
import com.okaforu.netwalk.game.logic.NetwalkGrid;
import com.okaforu.netwalk.models.Difficulty;
import com.okaforu.netwalk.models.GridLocation;
import com.okaforu.netwalk.models.Score;
import com.okaforu.netwalk.sql.ScoreSqlHelper;
import com.okaforu.netwalk.utils.SettingsPreference;
import com.okaforu.netwalk.views.GridTile;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    private SettingsFragment settingsFragment;
    private GamePausedFragment gamePausedFragment;

    private TableLayout tlGridTable;
    private AlertDialog gridSolvedDialog, highScoreDialog;
    private TextView tvTimeRemaining, tvMoves;

    private ScoreSqlHelper sqlHelper;
    private SettingsPreference settings;
    private SparseIntArray gridImageMap;
    private GameTimerTask timerTask;
    private TileEventHandler tileEventHandler;
    private NetwalkGrid netwalkGrid;

    private int gridColumns, gridRows, imageRatio, moves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initializeGridMap();
        initializeViews();
        restartGame();
    }

    private void initializeViews() {
        tlGridTable = (TableLayout) findViewById(R.id.tlGridTable);
        tvTimeRemaining = (TextView) findViewById(R.id.tvTimeRemaining);
        tvMoves = (TextView) findViewById(R.id.tvMoves);

        gamePausedFragment = new GamePausedFragment();
        gamePausedFragment.setOnItemClickedListener(new GamePausedFragmentListener());
        settingsFragment = SettingsFragment.newInstance(this, null);

        findViewById(R.id.ibPauseGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPause();
                gamePausedFragment.show(getSupportFragmentManager(), GamePausedFragment.TAG);
            }
        });

        initializeAlertDialogs();
    }

    private void initializeAlertDialogs() {
        //Game won alert dialog builder
        AlertDialog.Builder gameWonBuilder = new AlertDialog.Builder(this);
        gameWonBuilder.setView(R.layout.fragment_game_won);
        gameWonBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                restartGame();
            }
        });
        gameWonBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        gameWonBuilder.setCancelable(false);
        gridSolvedDialog = gameWonBuilder.create();


        //HighScore alert dialog buidler
        AlertDialog.Builder highScoreBuilder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.fragment_highscore_username, null);
        highScoreBuilder.setView(v);
        highScoreBuilder.setCancelable(false);

        final EditText etUsername = (EditText) v.findViewById(R.id.etUsername);
        v.findViewById(R.id.btnSaveUsername).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString();

                if (username.length() == 0) {
                    etUsername.setError("Username cannot be left empty");
                    return;
                }

                sqlHelper.saveScore(getGameScore(username));
                Toast.makeText(GameActivity.this, "Score successfully saved!", Toast.LENGTH_SHORT).show();
                highScoreDialog.dismiss();
            }
        });

        highScoreDialog = highScoreBuilder.create();
        highScoreDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                etUsername.setText(null);
            }
        });
    }

    /**
     * Initializes the instance members of this activity
     */
    private void initializeComponents() {
        moves = 0;
        initializeDifficulty();
        sqlHelper = new ScoreSqlHelper(this);
        timerTask = new GameTimerTask();
        tileEventHandler = new TileEventHandler();
        netwalkGrid = new NetwalkGrid(gridColumns, gridRows);
        new Timer().scheduleAtFixedRate(timerTask, 1000, 1000);
    }

    /**
     * Reads the saved difficulty and sets the game grid to use the specified configurations
     */
    private void initializeDifficulty() {
        settings = SettingsPreference.getInstance(this);
        Difficulty difficulty = settings.getSavedDifficulty();

        if (difficulty == Difficulty.HARD) {
            imageRatio = 110;
            gridColumns = 8;
            gridRows = 8;
        } else if (difficulty == Difficulty.NORMAL) {
            imageRatio = 48 * 3;
            gridColumns = 6;
            gridRows = 6;
        } else if (difficulty == Difficulty.EASY) {
            imageRatio = 92 * 2;
            gridColumns = 4;
            gridRows = 4;
        }
    }

    private void initializeGridMap() {
        gridImageMap = new SparseIntArray();

        gridImageMap.put(5, R.drawable.idle_pipe_ns);
        gridImageMap.put(69, R.drawable.active_pipe_ns);

        gridImageMap.put(10, R.drawable.idle_pipe_ew);
        gridImageMap.put(74, R.drawable.active_pipe_ew);

        gridImageMap.put(6, R.drawable.idle_pipe_ne);
        gridImageMap.put(70, R.drawable.active_pipe_ne);

        gridImageMap.put(3, R.drawable.idle_pipe_es);
        gridImageMap.put(67, R.drawable.active_pipe_es);

        gridImageMap.put(9, R.drawable.idle_pipe_sw);
        gridImageMap.put(73, R.drawable.active_pipe_sw);

        gridImageMap.put(12, R.drawable.idle_pipe_wn);
        gridImageMap.put(76, R.drawable.active_pipe_wn);

        gridImageMap.put(14, R.drawable.idle_pipe_new);
        gridImageMap.put(78, R.drawable.active_pipe_new);

        gridImageMap.put(7, R.drawable.idle_pipe_nes);
        gridImageMap.put(71, R.drawable.active_pipe_nes);

        gridImageMap.put(11, R.drawable.idle_pipe_esw);
        gridImageMap.put(75, R.drawable.active_pipe_esw);

        gridImageMap.put(13, R.drawable.idle_pipe_nsw);
        gridImageMap.put(77, R.drawable.active_pipe_nsw);

        gridImageMap.put(36, R.drawable.idle_terminal_n);
        gridImageMap.put(100, R.drawable.active_terminal_n);

        gridImageMap.put(34, R.drawable.idle_terminal_e);
        gridImageMap.put(98, R.drawable.active_terminal_e);

        gridImageMap.put(33, R.drawable.idle_terminal_s);
        gridImageMap.put(97, R.drawable.active_terminal_s);

        gridImageMap.put(40, R.drawable.idle_terminal_w);
        gridImageMap.put(104, R.drawable.active_terminal_w);

        gridImageMap.put(84, R.drawable.server_n);
        gridImageMap.put(82, R.drawable.server_e);
        gridImageMap.put(81, R.drawable.server_s);
        gridImageMap.put(88, R.drawable.server_w);

        gridImageMap.put(86, R.drawable.server_ne);
        gridImageMap.put(83, R.drawable.server_es);
        gridImageMap.put(89, R.drawable.server_sw);
        gridImageMap.put(92, R.drawable.server_wn);
        gridImageMap.put(85, R.drawable.server_ns);
        gridImageMap.put(90, R.drawable.server_ew);

        gridImageMap.put(94, R.drawable.server_new);
        gridImageMap.put(87, R.drawable.server_nes);
        gridImageMap.put(91, R.drawable.server_esw);
        gridImageMap.put(93, R.drawable.server_swn);

        gridImageMap.put(95, R.drawable.server_nesw);
    }

    /**
     * Randomizes game grid
     */
    private void randomizeGrid() {
        Random rand = new Random();

        //This section of the loop is to randomly rotate each grid element a random amount of times
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridColumns; col++) {
                int randomRotateAmount = rand.nextInt(5) + 1;
                for (int i = 0; i < randomRotateAmount; i++) {
                    netwalkGrid.rotateRight(col, row);
                }
            }
        }

        //Looping through each grid element again and making sure that they're not connected
        //This loop in not inside the previous loop to ensure a certain degree of randomness
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridColumns; col++) {
                if (! netwalkGrid.isServer(col, row)) {
                    int maxAttempts = 0;
                    while (maxAttempts < 4 && netwalkGrid.isConnected(col, row)) {
                        maxAttempts++;
                        netwalkGrid.rotateRight(col, row);
                    }
                }
            }
        }
    }

    private void restartGame() {
        if (timerTask != null) {
            timerTask.cancel();
        }

        initializeComponents();
        randomizeGrid();
        resetCounterAndTimerText();
        displayNetwalkGrid();
    }

    private Score getGameScore(String username) {
        return new Score(moves, username, timerTask.millisElapsed, settings.getSavedDifficulty());
    }

    private Bitmap getImage(int imageId) {
        return BitmapFactory.decodeResource(getResources(), imageId);
    }

    /**
     * Displays each grid element to the screen
     */
    private void displayNetwalkGrid() {
        tlGridTable.removeAllViews();

        for (int row = 0; row < gridRows; row++) {
            TableRow tableRow = new TableRow(this);

            for (int col = 0; col < gridColumns; col++) {
                int element = netwalkGrid.getGridElem(col, row);

                int key = gridImageMap.get(element, -1);
                if (key != -1) {
                    Bitmap image = getImage(key);

                    GridTile tile = new GridTile(this, new GridLocation(col, row));
                    Bitmap scaledImage = Bitmap.createScaledBitmap(image, imageRatio, imageRatio, true);
                    tile.setImageBitmap(scaledImage);
                    tile.setPadding(3, 3, 3, 3);
                    tile.setOnClickListener(tileEventHandler);
                    tableRow.addView(tile);
                }
            }

            tlGridTable.addView(tableRow);
        }
    }

    private void resetCounterAndTimerText() {
        moves = 0;
        timerTask.millisElapsed = 0;
        updateTimerDisplay();
        updateCounterDisplay();
    }

    private void updateTimerDisplay() {
        long minutes = (timerTask.millisElapsed % 3600) / 60;
        long seconds = timerTask.millisElapsed % 60;

        String timeElapsed = String.format(Locale.getDefault(), "Time: %dm %ds", minutes, seconds);
        tvTimeRemaining.setText(timeElapsed);
    }

    private void updateCounterDisplay() {
        tvMoves.setText(String.format(getString(R.string.game_moves_counter),moves));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerTask.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerTask.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerTask.pause();

        if (gridSolvedDialog != null) {
            gridSolvedDialog.dismiss();
        }

        if (highScoreDialog != null) {
            highScoreDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onPause();
        timerTask.pause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("moves", moves);
        outState.putLong("millis_elapsed", timerTask.millisElapsed);
        outState.putBoolean("game_paused", timerTask.paused);
        outState.putParcelable("netwalk_grid", netwalkGrid);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        moves = savedInstanceState.getInt("moves");
        timerTask.millisElapsed = savedInstanceState.getLong("millis_elapsed");
        netwalkGrid = savedInstanceState.getParcelable("netwalk_grid");

        //The game is able to restore the moves, timer and netwalk grid class
        //However I had problems with restoring the various alert dialog fragments
        //E.g. If the game is paused, I wanted it so when it's rotated it would maintain fragment state but couldn't quit get it to work properly

        updateCounterDisplay();
        updateTimerDisplay();
        displayNetwalkGrid();
    }

    /**
     * An class that handles all the events raised by the game paused fragment
     */
    private class GamePausedFragmentListener implements GamePausedFragment.PauseFragmentClickListener {

        @Override
        public void onRestartClicked() {
            gamePausedFragment.dismiss();
            restartGame();
        }

        @Override
        public void onResumeClicked() {
            onResume();
            gamePausedFragment.dismiss();
        }

        @Override
        public void onHomeClicked() {
            finish();
        }

        @Override
        public void onSettingsClicked() {
            settingsFragment.show(getSupportFragmentManager(), SettingsFragment.TAG);
        }

        @Override
        public void onViewScoresClicked() {
            startActivity(new Intent(GameActivity.this, HighScoreActivity.class));
        }
    }

    /**
     * A class that handles the event handlers for when a image tile is clicked
     */
    private class TileEventHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            moves++;

            GridTile clickedTile = (GridTile) view;
            GridLocation tileLocation = clickedTile.getGridLocation();
            netwalkGrid.rotateRight(tileLocation.getColumn(), tileLocation.getRow());
            updateCounterDisplay();

            if (netwalkGrid.isGridSolved()) {
                timerTask.cancel();
                gridSolvedDialog.show();

                if (sqlHelper.isHighScoreBeaten(getGameScore(null))) {
                    highScoreDialog.show();
                }
            }

            displayNetwalkGrid();
        }
    }
    
    private class GameTimerTask extends TimerTask {

        private long millisElapsed;
        private boolean paused;

        public GameTimerTask() {
            paused = false;
            millisElapsed = 0;
        }

        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (! paused) {
                        millisElapsed++;
                        updateTimerDisplay();
                    }
                }
            });
        }

        public void pause() {
            paused = true;
        }

        public void resume() {
            paused = false;
        }
    }
}