package eu.asteja.watchvolume;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "watch_volume_prefs";
    private static final String PREF_PROFILE = "profile_id";
    private static final String PREF_KEY_UP = "key_up";
    private static final String PREF_KEY_DOWN = "key_down";
    private static final String PREF_KEY_TOGGLE = "key_toggle";

    private static final int PROFILE_DEFAULT = 0;
    private static final int PROFILE_JIESHUO = 1;

    private static final int ACTION_NONE = 0;
    private static final int ACTION_ASSIGN_UP = 1;
    private static final int ACTION_ASSIGN_DOWN = 2;
    private static final int ACTION_ASSIGN_TOGGLE = 3;

    private AudioManager audioManager;
    private SharedPreferences prefs;
    private int currentStream = AudioManager.STREAM_ACCESSIBILITY;
    private int currentProfile = PROFILE_DEFAULT;

    private int keyUp;
    private int keyDown;
    private int keyToggle;
    private int captureAction = ACTION_NONE;

    private TextView modeText;
    private TextView statusText;
    private TextView keyUpLabel;
    private TextView keyDownLabel;
    private TextView keyToggleLabel;
    private TextView assignStatus;
    private TextView profileLabel;
    private Button toggleButton;
    private Button profileSwitchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        modeText = findViewById(R.id.modeText);
        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);
        keyUpLabel = findViewById(R.id.keyUpLabel);
        keyDownLabel = findViewById(R.id.keyDownLabel);
        keyToggleLabel = findViewById(R.id.keyToggleLabel);
        assignStatus = findViewById(R.id.assignStatus);
        profileLabel = findViewById(R.id.profileLabel);
        profileSwitchButton = findViewById(R.id.profileSwitchButton);

        Button assignUpButton = findViewById(R.id.assignUpButton);
        Button assignDownButton = findViewById(R.id.assignDownButton);
        Button assignToggleButton = findViewById(R.id.assignToggleButton);
        Button resetButton = findViewById(R.id.resetButton);

        findViewById(R.id.btnUp).setOnClickListener(v -> adjustVolume(1));
        findViewById(R.id.btnDown).setOnClickListener(v -> adjustVolume(-1));
        toggleButton.setOnClickListener(v -> toggleStream());

        assignUpButton.setOnClickListener(v -> beginAssign(ACTION_ASSIGN_UP));
        assignDownButton.setOnClickListener(v -> beginAssign(ACTION_ASSIGN_DOWN));
        assignToggleButton.setOnClickListener(v -> beginAssign(ACTION_ASSIGN_TOGGLE));
        resetButton.setOnClickListener(v -> resetAssignments());
        profileSwitchButton.setOnClickListener(v -> switchProfile());

        View root = findViewById(R.id.root);
        root.setOnGenericMotionListener((v, event) -> handleRotary(event));

        currentProfile = prefs.getInt(PREF_PROFILE, PROFILE_DEFAULT);
        loadAssignments();
        updateUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUi();
    }

    private String keyName(String baseKey) {
        return "p" + currentProfile + "_" + baseKey;
    }

    private void loadAssignments() {
        keyUp = prefs.getInt(keyName(PREF_KEY_UP), KeyEvent.KEYCODE_STEM_1);
        keyDown = prefs.getInt(keyName(PREF_KEY_DOWN), KeyEvent.KEYCODE_STEM_2);
        keyToggle = prefs.getInt(keyName(PREF_KEY_TOGGLE), KeyEvent.KEYCODE_STEM_3);
    }

    private void saveAssignment(int action, int keyCode) {
        SharedPreferences.Editor editor = prefs.edit();
        if (action == ACTION_ASSIGN_UP) {
            keyUp = keyCode;
            editor.putInt(keyName(PREF_KEY_UP), keyCode);
        } else if (action == ACTION_ASSIGN_DOWN) {
            keyDown = keyCode;
            editor.putInt(keyName(PREF_KEY_DOWN), keyCode);
        } else if (action == ACTION_ASSIGN_TOGGLE) {
            keyToggle = keyCode;
            editor.putInt(keyName(PREF_KEY_TOGGLE), keyCode);
        }
        editor.apply();
    }

    private void resetAssignments() {
        keyUp = KeyEvent.KEYCODE_STEM_1;
        keyDown = KeyEvent.KEYCODE_STEM_2;
        keyToggle = KeyEvent.KEYCODE_STEM_3;
        prefs.edit()
            .putInt(keyName(PREF_KEY_UP), keyUp)
            .putInt(keyName(PREF_KEY_DOWN), keyDown)
            .putInt(keyName(PREF_KEY_TOGGLE), keyToggle)
            .apply();
        captureAction = ACTION_NONE;
        updateUi();
    }

    private void beginAssign(int action) {
        captureAction = action;
        updateUi();
    }

    private void switchProfile() {
        currentProfile = (currentProfile == PROFILE_DEFAULT) ? PROFILE_JIESHUO : PROFILE_DEFAULT;
        prefs.edit().putInt(PREF_PROFILE, currentProfile).apply();
        captureAction = ACTION_NONE;
        loadAssignments();
        updateUi();
    }

    private void adjustVolume(int direction) {
        int adjust = direction > 0 ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER;
        audioManager.adjustStreamVolume(currentStream, adjust, AudioManager.FLAG_SHOW_UI);
        updateUi();
    }

    private void toggleStream() {
        currentStream = (currentStream == AudioManager.STREAM_ACCESSIBILITY)
            ? AudioManager.STREAM_MUSIC
            : AudioManager.STREAM_ACCESSIBILITY;
        updateUi();
    }

    private void updateUi() {
        String modeLabel = (currentStream == AudioManager.STREAM_ACCESSIBILITY)
            ? getString(R.string.mode_accessibility)
            : getString(R.string.mode_media);
        String toggleLabel = (currentStream == AudioManager.STREAM_ACCESSIBILITY)
            ? getString(R.string.toggle_to_media)
            : getString(R.string.toggle_to_accessibility);

        int volume = audioManager.getStreamVolume(currentStream);
        int max = audioManager.getStreamMaxVolume(currentStream);

        setVolumeControlStream(currentStream);
        modeText.setText(modeLabel);
        statusText.setText(getString(R.string.volume_status, volume, max));
        toggleButton.setText(toggleLabel);

        String profileName = (currentProfile == PROFILE_DEFAULT)
            ? getString(R.string.profile_default)
            : getString(R.string.profile_jieshuo);
        profileLabel.setText(getString(R.string.profile_title) + ": " + profileName);

        keyUpLabel.setText(getString(R.string.label_up, keyLabel(keyUp)));
        keyDownLabel.setText(getString(R.string.label_down, keyLabel(keyDown)));
        keyToggleLabel.setText(getString(R.string.label_toggle, keyLabel(keyToggle)));
        assignStatus.setText(
            captureAction == ACTION_NONE
                ? getString(R.string.assign_ready)
                : getString(R.string.assign_listen)
        );
    }

    private String keyLabel(int keyCode) {
        if (keyCode <= 0) {
            return getString(R.string.key_unassigned);
        }
        String raw = KeyEvent.keyCodeToString(keyCode);
        if (raw.startsWith("KEYCODE_")) {
            raw = raw.substring("KEYCODE_".length());
        }
        return raw.replace('_', ' ');
    }

    private boolean handleRotary(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_SCROLL
            && event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {
            float delta = event.getAxisValue(MotionEvent.AXIS_SCROLL);
            if (delta > 0) {
                adjustVolume(1);
            } else if (delta < 0) {
                adjustVolume(-1);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (captureAction != ACTION_NONE) {
            if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_POWER) {
                captureAction = ACTION_NONE;
                updateUi();
                return true;
            }
            saveAssignment(captureAction, keyCode);
            captureAction = ACTION_NONE;
            updateUi();
            return true;
        }

        if (keyCode == keyUp) {
            adjustVolume(1);
            return true;
        }
        if (keyCode == keyDown) {
            adjustVolume(-1);
            return true;
        }
        if (keyCode == keyToggle) {
            toggleStream();
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_DPAD_UP:
                adjustVolume(1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                adjustVolume(-1);
                return true;
            case KeyEvent.KEYCODE_STEM_PRIMARY:
            case KeyEvent.KEYCODE_ENTER:
                toggleStream();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
