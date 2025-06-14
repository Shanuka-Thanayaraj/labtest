package com.s23010188.shanuka;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity serves as the login screen for the application.
 * It features a video background, user input fields for username and password,
 * and interacts with a SQLite database for authentication.
 * Upon successful login, it navigates to the MapActivity.
 */
public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private TextureView textureViewBackground;
    private MediaPlayer mediaPlayer;

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureViewBackground = findViewById(R.id.textureViewBackground);
        textureViewBackground.setSurfaceTextureListener(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        databaseHelper = new DatabaseHelper(this);

        buttonLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!databaseHelper.doesUsernameExist(username)) {
            if (databaseHelper.addUser(username, password)) {
                Toast.makeText(this, "New user registered successfully! Logging in...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MapActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to register user. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (databaseHelper.checkUser(username, password)) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MapActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Wrong password.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // TextureView Callbacks

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        mediaPlayer = new MediaPlayer();

        try {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.login_background);
            mediaPlayer.setDataSource(this, videoUri);
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0f, 0f); // mute

            mediaPlayer.setOnPreparedListener(mp -> {
                adjustVideoScaling(mp, textureViewBackground);
                mp.start();
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adjustVideoScaling(MediaPlayer mp, TextureView textureView) {
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();

        float scaleX = (float) viewWidth / videoWidth;
        float scaleY = (float) viewHeight / videoHeight;
        float scale = Math.max(scaleX, scaleY);

        float scaledWidth = scale * videoWidth;
        float scaledHeight = scale * videoHeight;

        float pivotX = viewWidth / 2f;
        float pivotY = viewHeight / 2f;

        Matrix matrix = new Matrix();
        matrix.setScale(scaledWidth / videoWidth, scaledHeight / videoHeight, pivotX, pivotY);
        textureView.setTransform(matrix);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
