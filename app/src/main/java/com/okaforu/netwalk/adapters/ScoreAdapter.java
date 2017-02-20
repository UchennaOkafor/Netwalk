package com.okaforu.netwalk.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.okaforu.netwalk.R;
import com.okaforu.netwalk.models.Score;

import java.util.List;
import java.util.Locale;

/**
 * A score adapter for displaying high scores using a recycler view
 */
public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private List<Score> scores;

    @Override
    public ScoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.score_row, parent, false);
        v.setPadding(0, 10, 0, 10);
        return new ScoreViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ScoreViewHolder holder, int position) {
        holder.setScoreRow(scores.get(position));
    }

    @Override
    public int getItemCount() {
        return scores == null ? 0 : scores.size();
    }

    public void replaceScores(List<Score> scores) {
        this.scores = scores;
        notifyDataSetChanged();
    }

    public class ScoreViewHolder extends RecyclerView.ViewHolder {

        private TextView etRank, etUsername, etMoves, etTimeTaken;

        public ScoreViewHolder(View itemView) {
            super(itemView);
            initializeWidgets(itemView);
        }

        private void initializeWidgets(View itemView) {
            etRank = (TextView) itemView.findViewById(R.id.etRank);
            etUsername = (TextView) itemView.findViewById(R.id.etUsername);
            etMoves = (TextView) itemView.findViewById(R.id.etMoves);
            etTimeTaken = (TextView) itemView.findViewById(R.id.etTimeTaken);

            etRank.setPaddingRelative(10, 0, 10, 0);
            etUsername.setPaddingRelative(10, 0, 10, 0);
            etMoves.setPaddingRelative(10, 0, 10, 0);
        }

        public void setScoreRow(Score score) {
            etUsername.setText(score.getUsername());
            etRank.setText(String.format(Locale.getDefault(), "%d", score.getRank()));
            etMoves.setText(String.format(Locale.getDefault(), "%d", score.getMoves()));

            int minutes = (int) ((score.getSecondsTaken() % 3600) / 60);
            int seconds = (int) (score.getSecondsTaken() % 60);
            String timeElapsed = "";

            if (minutes > 0) {
                timeElapsed = String.format(Locale.getDefault(), "%dm", minutes);
            }

            timeElapsed += String.format(Locale.getDefault(), " %ds", seconds);
            etTimeTaken.setText(timeElapsed);
        }
    }
}