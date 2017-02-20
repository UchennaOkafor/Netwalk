package com.okaforu.netwalk.sql;

import android.content.Context;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.okaforu.netwalk.models.Difficulty;
import com.okaforu.netwalk.models.Score;

import java.util.ArrayList;
import java.util.List;

public class ScoreSqlHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "scores.db";

    private static final String TABLE_NAME = "scores";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_MOVES = "moves";
    private static final String COLUMN_TIME_TAKEN = "time_taken";
    private static final String COLUMN_DIFFICULTY =  "difficulty";

    public ScoreSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = String.format("CREATE TABLE %s " +
                "(%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "%s VARCHAR(30), " +
                "%s INTEGER, " +
                "%s INTEGER, " +
                "%s VARCHAR(6));", TABLE_NAME, COLUMN_ID, COLUMN_USERNAME, COLUMN_MOVES, COLUMN_TIME_TAKEN, COLUMN_DIFFICULTY);

        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(query);
        onCreate(sqLiteDatabase);
    }

    public void saveScore(Score score) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, score.getUsername());
        values.put(COLUMN_MOVES, score.getMoves());
        values.put(COLUMN_TIME_TAKEN, score.getSecondsTaken());
        values.put(COLUMN_DIFFICULTY, score.getDifficulty().toString());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, values);

        db.close();
    }

    /**
     * Get's a list of high scores based on the difficulty level
     * @param targetDifficulty the difficulty to retrieve scores by
     * @return a list of all high scores
     */
    public List<Score> getAllHighScores(Difficulty targetDifficulty) {
        SQLiteDatabase db = getWritableDatabase();
        //Scores are ordered by two columns, whereby time taken holds priority over moves
        String query = String.format("SELECT * FROM %s WHERE difficulty = '%s' ORDER BY %s ASC, %s ASC",
                                    TABLE_NAME, targetDifficulty.toString(), COLUMN_TIME_TAKEN, COLUMN_MOVES);

        Cursor cursor = db.rawQuery(query, null);
        List<Score> scores = new ArrayList<>();
        int rank = 0;

        while (cursor.moveToNext()) {
            int moves = cursor.getInt(cursor.getColumnIndex(COLUMN_MOVES));
            String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            long timeTaken = cursor.getLong(cursor.getColumnIndex(COLUMN_TIME_TAKEN));
            String difficulty = cursor.getString(cursor.getColumnIndex(COLUMN_DIFFICULTY));

            Score score = new Score(++rank, moves, username, timeTaken, Difficulty.valueOf(difficulty));
            scores.add(score);
        }

        cursor.close();
        db.close();

        return scores;
    }

    /**
     * A method to check if the current score from users most recent game is better than the current high score
     * @param score the users current score
     * @return boolean result of operation
     */
    public boolean isHighScoreBeaten(Score score) {
        SQLiteDatabase db = getWritableDatabase();
        //Scores are ordered by two columns, whereby time taken holds priority over moves
        String query = String.format("SELECT * FROM %s WHERE difficulty = '%s' ORDER BY %s ASC, %s ASC LIMIT 1",
                                    TABLE_NAME, score.getDifficulty().toString(), COLUMN_TIME_TAKEN, COLUMN_MOVES);
        Cursor cursor = db.rawQuery(query, null);

        boolean highScoreBeaten = true;

        if (cursor.moveToFirst()) {
            int moves = cursor.getInt(cursor.getColumnIndex(COLUMN_MOVES));
            long timeTaken = cursor.getLong(cursor.getColumnIndex(COLUMN_TIME_TAKEN));
            highScoreBeaten = score.getSecondsTaken() <= timeTaken && score.getMoves() <= moves;
        }

        cursor.close();
        db.close();

        return highScoreBeaten;
    }
}