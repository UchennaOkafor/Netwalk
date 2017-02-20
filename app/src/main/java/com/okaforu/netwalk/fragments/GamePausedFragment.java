package com.okaforu.netwalk.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.okaforu.netwalk.R;

/**
 * A dialog fragment to be displayed when the game is paused
 */
public class GamePausedFragment extends DialogFragment {

    public final static String TAG = "GamePausedFragment";
    private PauseFragmentClickListener itemClickedListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        setRetainInstance(true);

        View view  = getActivity().getLayoutInflater().inflate(R.layout.fragment_game_paused, null);
        initializeImageButtonEvents(view);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        return dialogBuilder.setView(view).create();
    }

    public void setOnItemClickedListener(PauseFragmentClickListener listener) {
        this.itemClickedListener = listener;
    }

    private void initializeImageButtonEvents(View view) {
        view.findViewById(R.id.btnRestart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    itemClickedListener.onRestartClicked();
                }
            }
        });

        view.findViewById(R.id.btnResume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    itemClickedListener.onResumeClicked();
                }
            }
        });

        view.findViewById(R.id.ibHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    itemClickedListener.onHomeClicked();
                }
            }
        });

        view.findViewById(R.id.ibSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    itemClickedListener.onSettingsClicked();
                }
            }
        });

        view.findViewById(R.id.ibViewScores).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    itemClickedListener.onViewScoresClicked();
                }
            }
        });
    }

    public interface PauseFragmentClickListener {
        void onRestartClicked();
        void onResumeClicked();
        void onHomeClicked();
        void onSettingsClicked();
        void onViewScoresClicked();
    }
}