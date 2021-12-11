package com.eysoft.a8puzzle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import AI.AStar;
import AI.HeuristicManhattan;
import AI.Solver;
import AI.SolverMemoDecorator;
import model.Board;
import view.BoardView;
import view.Piece;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public BoardView boardView;
    public TextView moveView;
    public TextView goalView;
    public Button shuffle;
    public Button solve;
    //public Button skipAhead;

    public TextView timerTextView;
    public TextView highScoreView;

    public Board goal;
    public Solver solver;

    // Set in boardView once view width is set
    public TranslateAnimation outBoard;
    public TranslateAnimation inBoard;

    private static final int DIALOG_ABOUT = 0;
    public static final int DIALOG_WIN = 1;

    private static final float SHAKE_THRESHOLD = 3.25f; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private long mLastShakeTime;
    private SensorManager mSensorMgr;

    SharedPreferences preferences;

    //<editor-fold desc = "Pref Keys" defaultstate = "collapsed">
    public final static String darkModeKey = "darkmode_switch_pref";
    public final static String enableTimePrefKey = "display_time_pref";
    public final static String gridSizePrefKey = "grid_size";
    public final String autoDarkModeCheckBokPrefKey = "auto_dark_pref_key";
    //</editor-fold>


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //<editor-fold defaultstate = "collapsed" desc = "Variables init">
        boardView = (BoardView) findViewById(R.id.boardView);
        moveView = (TextView) findViewById(R.id.moves);
        goalView = (TextView) findViewById(R.id.goal);
        shuffle = (Button) findViewById(R.id.shuffle);
        solve = (Button) findViewById(R.id.solveBtn);
        timerTextView = (TextView) findViewById(R.id.timerTextView);
        highScoreView = findViewById(R.id.highScoreTextView);
        //        skipAhead = (Button) findViewById(R.id.skipAhead);

        //</editor-fold>

        goal = new Board("123 456 780");
        solver = new SolverMemoDecorator(new AStar(goal, new HeuristicManhattan(goal)));
        setupBoardView();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //<editor-fold defaultstate="collapsed" desc="Shake Sensor">
        // Get a sensor manager to listen for shakes
        mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Listen for shakes
        Sensor accelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorMgr.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        //</editor-fold>

        //<editor-fold desc = "Dark Mode Switching" defaultstate = "collapsed">
        boolean autoDark  = preferences.getBoolean("auto_dark_pref_key", true);

        if (autoDark){
            preferences.edit().putBoolean("darkmode_switch_pref",false).apply();
            int nightModeFlags = getBaseContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setTheme(R.style.DarkTheme);
                    setDarkMode(true);
                    break;

                case Configuration.UI_MODE_NIGHT_NO:
                    setTheme(R.style.AppTheme);
                    setDarkMode(false);
                    break;

                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    break;
            }
        }else {
            boolean prefDarkModeSwitchEnabled = preferences.getBoolean("darkmode_switch_pref", false);

            if (prefDarkModeSwitchEnabled){
                setTheme(R.style.StandardDark);
                setDarkMode(true);
                setActivityBackgroundColor(Color.rgb(40,40,40));
            }else {
                setTheme(R.style.StandardLight);
                setDarkMode(false);
            }
        }
        //</editor-fold>

        String gridSelect = preferences.getString(gridSizePrefKey, "3x3");
        Log.d(TAG, "grid size settings "+ gridSelect);

        int highScore  = preferences.getInt(BoardView.highScoreKey, 0);
        highScoreView.setText("High Score: "+highScore);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //updateUI(account);

    }

    public void showMyDialog(int dialog) {
        switch (dialog){
            case DIALOG_ABOUT:
                break;
            case DIALOG_WIN:
                DialogFragment win = new WinFragment();
                win.show(getSupportFragmentManager(), "Win Fragment");
                break;
        }
    }

    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }

    public void gridSizeChangeOnClick(MenuItem item) {
        DialogFragment grid_select = new GridSelectFragment();
        grid_select.show(getSupportFragmentManager(), "Grid Select");
    }

    public void logInMenuClicked(MenuItem item) {
        final LoginFragment loginFragment = new LoginFragment();
        loginFragment.show(getSupportFragmentManager(), "login_fragment");
    }

    public static void setDarkMode(boolean mode){
        if (mode){
            Piece.colors = new int[]{Color.rgb(34, 34, 34), Color.rgb(34, 34, 34)};
            Piece.shadowColor = Color.rgb(22,22,22);
        }
        else {
            Piece.colors = new int[]{Color.rgb(175, 13, 13),Color.rgb(175, 13, 13), Color.rgb(213, 10, 219)};;
            Piece.shadowColor = Color.rgb(192,192,192);
        }
    }

    public void setupBoardView() {
        shuffle.setOnClickListener(boardView);
        solve.setOnClickListener(boardView);
//        skipAhead.setOnClickListener(boardView);
        boardView.setupPuzzle();
    }

    public void menuItemOnClick(MenuItem item) {

        switch (item.getItemId()){
            case R.id.settingsMenu:
                Intent intent = new Intent(this,SettingsActivity.class);
                MainActivity.this.startActivity(intent);
                break;
            case R.id.aboutMenu:
                Intent intent_about = new Intent(this,AboutActivity.class);
                MainActivity.this.startActivity(intent_about);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 40) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment loginDialog = fragmentManager.findFragmentByTag("login_fragment");
            DialogFragment dialog = (DialogFragment) loginDialog;
            dialog.dismiss();
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    public void updateUI(GoogleSignInAccount account){
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming activity...");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing activity...");
        super.onPause();
        boardView.pause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                double acceleration = Math.sqrt(Math.pow(x, 2) +
                        Math.pow(y, 2) +
                        Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime;
                    Log.d("8puzzle", "Shake, Rattle, and Roll");
                    Toast.makeText(this, "Shuffled", Toast.LENGTH_SHORT).show();
                    boardView.newPuzzle("123 456 780");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}