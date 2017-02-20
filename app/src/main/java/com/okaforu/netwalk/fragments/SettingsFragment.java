package com.okaforu.netwalk.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.okaforu.netwalk.R;
import com.okaforu.netwalk.models.Difficulty;
import com.okaforu.netwalk.utils.SettingsPreference;

/**
 * A fragment class to handle user configurable persistent settings
 */
public class SettingsFragment extends DialogFragment {

    public final static String TAG = "SettingsFragment";

    private RadioButton rbEasy, rbNormal, rbHard;
    private SettingsPreference settings;
    private Class<?> nextActivityClass;

    public static SettingsFragment newInstance(Context context, Class<?> nextClass) {
        SettingsFragment frag = new SettingsFragment();
        frag.settings = SettingsPreference.getInstance(context);
        frag.nextActivityClass = nextClass;
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //If the attached activity is restored, it can cause the settings variable to be null
        settings = SettingsPreference.getInstance(getContext());
        setRetainInstance(true);

        View view  = getActivity().getLayoutInflater().inflate(R.layout.fragment_difficulty_settings, null);
        initializeUIComponents(view);
        initializeRadioGroup();

        return new AlertDialog.Builder(getContext()).setView(view).create();
    }

    private void initializeRadioGroup() {
        if (settings.isSettingExists()) {
            Difficulty difficultyMode = settings.getSavedDifficulty();

            if (difficultyMode == Difficulty.EASY) {
                rbEasy.setChecked(true);
            } else if (difficultyMode == Difficulty.NORMAL) {
                rbNormal.setChecked(true);
            } else if (difficultyMode == Difficulty.HARD) {
                rbHard.setChecked(true);
            }
        }
    }

    private void initializeUIComponents(View view) {
        view.findViewById(R.id.btnSaveDifficulty).setOnClickListener(new SaveButtonHandler());
        rbEasy = (RadioButton) view.findViewById(R.id.rbEasy);
        rbNormal = (RadioButton) view.findViewById(R.id.rbNormal);
        rbHard = (RadioButton) view.findViewById(R.id.rbHard);
    }

    private Difficulty getSelectedDifficulty() {
        if (rbEasy.isChecked()) {
            return Difficulty.EASY;
        } else if (rbNormal.isChecked()) {
            return Difficulty.NORMAL;
        } else if (rbHard.isChecked()) {
            return Difficulty.HARD;
        }

        return null;
    }

    private class SaveButtonHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Difficulty difficulty = getSelectedDifficulty();

            if (difficulty == null) {
                Toast.makeText(getContext(), "Difficulty cannot be left empty", Toast.LENGTH_SHORT).show();
                return;
            }

            settings.saveDifficulty(difficulty);
            String toastMsg = "Settings has been successfully saved";
            int toastLength = Toast.LENGTH_SHORT;

            if (nextActivityClass == null) {
                toastMsg = "Settings saved, changes will apply the next game round.";
                toastLength = Toast.LENGTH_LONG;
            }

            Toast.makeText(getContext(), toastMsg, toastLength).show();
            dismiss();

            if (nextActivityClass != null) {
                startActivity(new Intent(view.getContext(), nextActivityClass));
            }
        }
    }
}