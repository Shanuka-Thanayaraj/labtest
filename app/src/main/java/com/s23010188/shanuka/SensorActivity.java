package com.s23010188.shanuka; // REPLACE WITH YOUR ACTUAL PACKAGE NAME

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Locale; // Added import for Locale

/**
 * SensorActivity monitors the ambient temperature sensor and plays an audio alert
 * if the temperature exceeds a predefined threshold. It also updates the UI with
 * the current temperature and provides a visual indicator for the alert.
 */
public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor temperatureSensor;
    private TextView textViewTemperature;
    private TextView textViewThresholdStatus;
    private ImageView imageViewWarning;
    private Button buttonBackToMap;
    private MediaPlayer mediaPlayer;

    // Temperature threshold (e.g., last two digits of SID S92010188 -> 88)
    private static final float TEMPERATURE_THRESHOLD = 88.0f; // Example threshold in Celsius

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor); // Set the layout for this activity

        // Initialize UI components
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewThresholdStatus = findViewById(R.id.textViewThresholdStatus);
        imageViewWarning = findViewById(R.id.imageViewWarning);
        buttonBackToMap = findViewById(R.id.buttonBackToMap);

        // Initialize SensorManager and get the ambient temperature sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            if (temperatureSensor == null) {
                // Handle case where ambient temperature sensor is not available
                Toast.makeText(this, "Ambient Temperature Sensor not available on this device.", Toast.LENGTH_LONG).show();
                textViewTemperature.setText("N/A");
            }
        } else {
            Toast.makeText(this, "Sensor service not available.", Toast.LENGTH_LONG).show();
            textViewTemperature.setText("N/A");
        }

        // Initialize MediaPlayer for audio alerts
        mediaPlayer = new MediaPlayer();
        setupMediaPlayer();

        // Update threshold status text
        textViewThresholdStatus.setText("Threshold: " + TEMPERATURE_THRESHOLD + "°C (No alert)");

        // Set up back button click listener
        buttonBackToMap.setOnClickListener(v -> finish()); // Go back to the previous activity (MapActivity)
    }

    /**
     * Sets up the MediaPlayer to load the audio alert from raw resources.
     */
    private void setupMediaPlayer() {
        try {
            // Load audio file from res/raw folder
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.threshold_alert);
            if (afd == null) {
                Toast.makeText(this, "Audio file not found: threshold_alert.mp3", Toast.LENGTH_SHORT).show();
                return;
            }
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare(); // Prepare the media player asynchronously
            mediaPlayer.setLooping(false); // Do not loop the sound
            afd.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading audio file.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the activity resumes
        if (temperatureSensor != null) {
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener when the activity pauses to save battery
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        // Stop and reset MediaPlayer if it's playing
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources when the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Called when sensor values have changed.
     *
     * @param event The SensorEvent object that contains the new sensor data.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            float temperature = event.values[0]; // Get the temperature value
            // Using Locale.getDefault() for correct number formatting based on user's device locale
            textViewTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", temperature));

            // Check if temperature exceeds the threshold
            if (temperature > TEMPERATURE_THRESHOLD) {
                textViewThresholdStatus.setText(String.format(Locale.getDefault(), "Threshold: %.1f°C (Alert!)", TEMPERATURE_THRESHOLD));
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start(); // Play alert sound
                }
                // Show and animate warning icon
                imageViewWarning.setVisibility(View.VISIBLE);
                startBlinkAnimation(imageViewWarning);
            } else {
                textViewThresholdStatus.setText(String.format(Locale.getDefault(), "Threshold: %.1f°C (No alert)", TEMPERATURE_THRESHOLD));
                // Stop sound and hide warning icon if temperature drops below threshold
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset(); // Reset to allow re-preparation if needed
                    setupMediaPlayer(); // Re-prepare the media player
                }
                imageViewWarning.setVisibility(View.GONE);
                imageViewWarning.clearAnimation();
            }
        }
    }

    /**
     * Called when the accuracy of the sensor has changed.
     *
     * @param sensor   The sensor in question.
     * @param accuracy The new accuracy of this sensor, one of SensorManager.SENSOR_STATUS_*.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example, but can be implemented if sensor accuracy changes need to be handled.
    }

    /**
     * Starts a blinking animation for the given view.
     * @param view The view to animate.
     */
    private void startBlinkAnimation(View view) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500); // 0.5 seconds
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
    }
}
