package com.dekhi.dekhi.ui.player;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.ui.PlayerView;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.util.ThemeHelper;

@OptIn(markerClass = UnstableApi.class)
public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    public static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";

    private PlayerView playerView;
    private ExoPlayer player;
    private View layoutBuffering;
    private TextView tvBufferingTelemetry;
    private TextView tvGestureStatus;
    private AudioManager audioManager;
    private GestureDetector gestureDetector;
    
    private ImageButton btnNext, btnPrevious, btnBack, btnFavorite;
    private TextView tvTitle;
    
    private PlayerViewModel viewModel;
    
    private boolean doubleBackToExitPressedOnce = false;
    private final Handler mBackHandler = new Handler(Looper.getMainLooper());
    private final Runnable mBackRunnable = () -> doubleBackToExitPressedOnce = false;

    private boolean playWhenReady = true;
    private final Handler mBufferingTimeoutHandler = new Handler(Looper.getMainLooper());
    private final Runnable mBufferingTimeoutRunnable = () -> {
        if (player != null && player.getPlaybackState() == Player.STATE_BUFFERING) {
            tvBufferingTelemetry.setText("Stream link is unresponsive. Try another channel?");
        }
    };

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            if (player != null) player.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (player != null) player.play();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ThemeHelper.isDynamicColorsEnabled(this)) {
            com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(this);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (ThemeHelper.isAmoledMode(this)) {
            getWindow().getDecorView().setBackgroundColor(android.graphics.Color.BLACK);
            findViewById(R.id.player_view).setBackgroundColor(android.graphics.Color.BLACK);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        playerView = findViewById(R.id.player_view);
        layoutBuffering = findViewById(R.id.layout_buffering);
        tvBufferingTelemetry = findViewById(R.id.tv_buffering_telemetry);
        tvGestureStatus = findViewById(R.id.tv_gesture_status);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == View.VISIBLE) {
                showSystemUI();
            } else {
                hideSystemUI();
            }
        });

        btnNext = playerView.findViewById(R.id.btn_next);
        btnPrevious = playerView.findViewById(R.id.btn_previous);
        btnBack = playerView.findViewById(R.id.btn_back);
        btnFavorite = playerView.findViewById(R.id.btn_favorite);
        tvTitle = playerView.findViewById(R.id.tv_title);

        setupExoPlayer();
        setupControlListeners();
        setupGestures();
        setupObservers();
        hideSystemUI();

        if (savedInstanceState == null) {
            long channelId = getIntent().getLongExtra(EXTRA_CHANNEL_ID, -1);
            long playlistId = getIntent().getLongExtra(EXTRA_PLAYLIST_ID, -1);
            String initialUrl = getIntent().getStringExtra(EXTRA_URL);
            String initialName = getIntent().getStringExtra(EXTRA_NAME);
            
            if (playlistId != -1) {
                viewModel.loadPlaylist(playlistId, channelId);
            } else if (initialUrl != null) {
                playStream(initialUrl, initialName);
            }
        }
    }

    private void hideSystemUI() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

    private void showSystemUI() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && (playerView == null || !playerView.isControllerFullyVisible())) {
            hideSystemUI();
        }
    }

    private void setupObservers() {
        viewModel.getCurrentChannel().observe(this, channel -> {
            if (channel != null) {
                playStream(channel.getStreamUrl(), channel.getName());
                updateFavoriteIcon(channel.isFavorite());
            }
        });
    }

    private void setupExoPlayer() {
        player = viewModel.getPlayer();
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                updateBufferingUI(playbackState);
            }

            @Override
            public void onPlayerError(androidx.media3.common.PlaybackException error) {
                Log.e("IPTV_DEBUG", "ExoPlayer Error: " + error.getMessage());
                showErrorDialog();
            }
        });
    }

    private void updateBufferingUI(int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            layoutBuffering.setVisibility(View.VISIBLE);
            
            mBufferingTimeoutHandler.removeCallbacks(mBufferingTimeoutRunnable);
            mBufferingTimeoutHandler.postDelayed(mBufferingTimeoutRunnable, 15000);
            
            long bitrate = DefaultBandwidthMeter.getSingletonInstance(PlayerActivity.this).getBitrateEstimate();
            if (bitrate > 0) {
                double mbps = bitrate / 8000000.0;
                if (mbps >= 5.0) {
                    tvBufferingTelemetry.setText("Optimizing stream...");
                } else if (mbps < 0.5) {
                    tvBufferingTelemetry.setText("Slow network connection...");
                } else {
                    tvBufferingTelemetry.setText("Loading...");
                }
            } else {
                tvBufferingTelemetry.setText("Connecting...");
            }
        } else {
            layoutBuffering.setVisibility(View.GONE);
            mBufferingTimeoutHandler.removeCallbacks(mBufferingTimeoutRunnable);
        }
    }

    private void playStream(String url, String name) {
        if (url == null) return;
        if (tvTitle != null) tvTitle.setText(name);

        MediaItem currentItem = player.getCurrentMediaItem();
        if (currentItem != null && currentItem.localConfiguration != null 
                && url.equals(currentItem.localConfiguration.uri.toString())) {
            return;
        }
        
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(url))
                .setMimeType(url.contains(".m3u8") ? MimeTypes.APPLICATION_M3U8 : null)
                .build();
        
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    private void setupControlListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
        
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> viewModel.playNext());
        }

        if (btnPrevious != null) {
            btnPrevious.setOnClickListener(v -> viewModel.playPrevious());
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> viewModel.toggleFavorite());
        }
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (btnFavorite != null) {
            btnFavorite.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        }
    }

    private void showErrorDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Playback Error")
                .setMessage("Sorry, this channel is currently unreachable. Please try another channel!")
                .setCancelable(false)
                .setPositiveButton("NEXT CHANNEL", (dialog, which) -> viewModel.playNext())
                .setNegativeButton("OK / BACK", (dialog, which) -> finish())
                .show();
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e1 == null || e2 == null) return false;
                
                float deltaY = e1.getY() - e2.getY();
                float width = playerView.getWidth();
                float height = playerView.getHeight();
                
                if (e1.getX() < width * 0.45f) {
                    adjustBrightness(deltaY / height);
                } else if (e1.getX() > width * 0.55f) {
                    adjustVolume(deltaY / height);
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (playerView.isControllerFullyVisible()) {
                    playerView.hideController();
                } else {
                    playerView.showController();
                }
                return true;
            }
        });

        playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                tvGestureStatus.setVisibility(View.GONE);
            }
            return true;
        });
    }

    private void adjustVolume(float percent) {
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int delta = (int) (percent * maxVol * 1.5f);
        int newVol = Math.max(0, Math.min(maxVol, currentVol + delta));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);
        showGestureStatus("Volume: " + (newVol * 100 / maxVol) + "%");
    }

    private void adjustBrightness(float percent) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float brightness = lp.screenBrightness;
        if (brightness < 0) brightness = 0.5f;
        lp.screenBrightness = Math.max(0.01f, Math.min(1.0f, brightness + percent * 1.5f));
        getWindow().setAttributes(lp);
        showGestureStatus("Brightness: " + (int) (lp.screenBrightness * 100) + "%");
    }

    private void showGestureStatus(String text) {
        tvGestureStatus.setText(text);
        tvGestureStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            cleanupBackHandler();
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap Back once more to close stream", Toast.LENGTH_SHORT).show();

        mBackHandler.postDelayed(mBackRunnable, 2000);
    }

    private void cleanupBackHandler() {
        if (mBackHandler != null && mBackRunnable != null) {
            mBackHandler.removeCallbacks(mBackRunnable);
        }
    }

    private void requestAudioFocus() {
        if (audioManager != null) {
            audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestAudioFocus();
        if (player != null) {
            player.setPlayWhenReady(playWhenReady);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (player != null) {
            player.setPlayWhenReady(playWhenReady);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            player.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cleanupBackHandler();
        doubleBackToExitPressedOnce = false;
        if (player != null) {
            player.pause();
            if (isFinishing()) {
                player.stop();
            }
        }
    }

    @Override
    protected void onDestroy() {
        cleanupBackHandler();
        mBufferingTimeoutHandler.removeCallbacks(mBufferingTimeoutRunnable);
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playerView.setPlayer(null);
            player = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (player != null && player.isPlaying()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                enterPictureInPictureMode(new android.app.PictureInPictureParams.Builder().build());
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, android.content.res.Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        playerView.setUseController(!isInPictureInPictureMode);
    }
}
