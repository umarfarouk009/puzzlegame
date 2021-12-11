package com.eysoft.a8puzzle;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String darkModeKey = "darkmode_switch_pref";
    public final static String enableTimePrefKey = "display_time_pref";
    public final static String gridSizePrefKey = "grid_size";
    public final String autoDarkModeCheckBokPrefKey = "auto_dark_pref_key";

    public static boolean isTimerEnabled = false;

    MainActivity mainGameActivity;

    SwitchPreference darkModeSwitch;
    SwitchPreference enableTimerSwitch;
    CheckBoxPreference autoDarkCheckBox;
    ListPreference gridSizeSelectList;

    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(darkModeKey)){
            if (darkModeSwitch.isChecked()){
                sharedPreferences.edit().putBoolean(darkModeKey, true).apply();
                getContext().setTheme(R.style.DarkTheme);
                MainActivity.setDarkMode(true);
            }else{
                sharedPreferences.edit().putBoolean(darkModeKey, false).apply();
                getContext().setTheme(R.style.AppTheme);
                MainActivity.setDarkMode(false);
            }
        }
        else if (key.equals(SettingsFragment.enableTimePrefKey)){
            if (enableTimerSwitch.isChecked()){
                sharedPreferences.edit().putBoolean(enableTimePrefKey, true).apply();
                isTimerEnabled = true;
            }
            else {
                sharedPreferences.edit().putBoolean(enableTimePrefKey, false).apply();
                isTimerEnabled = false;
            }
        }
        else if (key.equals(autoDarkModeCheckBokPrefKey)){
            if (autoDarkCheckBox.isChecked()){
                sharedPreferences.edit().putBoolean(autoDarkModeCheckBokPrefKey, true).apply();
            }
            else {
                sharedPreferences.edit().putBoolean(autoDarkModeCheckBokPrefKey, false).apply();
            }
        }
        else if (key.equals(gridSizePrefKey)){
            if (gridSizeSelectList.getValue().equals("3x3")){
                mainGameActivity.boardView.newPuzzle("123 456 780");
            }
            else if (gridSizeSelectList.getValue().equals("4x4")){
                mainGameActivity.boardView.newPuzzle("1234 5678 9ABC DEF0");
            }
        }
    }
}