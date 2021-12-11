package com.eysoft.a8puzzle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;


public class SettingsActivity extends AppCompatActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(findViewById(R.id.settings_fragment) != null){
            if (savedInstanceState != null)
                return;
            getSupportFragmentManager().beginTransaction().add(R.id.settings_fragment, new SettingsFragment()).commit();
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //<editor-fold desc = "Dark Mode Switching" defaultstate = "collapsed">
        boolean autoDark  = preferences.getBoolean("auto_dark_pref_key", true);

        if (autoDark){
            preferences.edit().putBoolean("darkmode_switch_pref",false).apply();
            int nightModeFlags = getBaseContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setTheme(R.style.DarkTheme);
                    break;

                case Configuration.UI_MODE_NIGHT_NO:
                    setTheme(R.style.AppTheme);
                    break;

                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    break;
            }
        }else {
            boolean prefDarkModeSwitchEnabled = preferences.getBoolean("darkmode_switch_pref", false);

            if (prefDarkModeSwitchEnabled){
                setTheme(R.style.StandardDark);
            }else {
                setTheme(R.style.StandardLight);
            }
        }
        //</editor-fold>

    }
}