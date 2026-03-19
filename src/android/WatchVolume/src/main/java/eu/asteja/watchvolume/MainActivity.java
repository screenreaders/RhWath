package eu.asteja.watchvolume;

import android.content.Context;
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
    private AudioManager audioManager;
    private int currentStream = AudioManager.STREAM_ACCESSIBILITY;
    private TextView modeText;
    private TextView statusText;
    private Button toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        modeText = findViewById(R.id.modeText);
        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);

        findViewById(R.id.btnUp).setOnClickListener(v -> adjustVolume(1));
        findViewById(R.id.btnDown).setOnClickListener(v -> adjustVolume(-1));
        toggleButton.setOnClickListener(v -> toggleStream());

        View root = findViewById(R.id.root);
        root.setOnGenericMotionListener((v, event) -> handleRotary(event));

        updateUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_STEM_1:
                adjustVolume(1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_STEM_2:
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
