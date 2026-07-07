package com.dekhi.dekhi.ui.player;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.data.AppDatabase;
import com.dekhi.dekhi.data.entity.Channel;

import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = UnstableApi.class)
public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    public static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";

    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar pbLoading;
    private TextView tvGestureStatus;
    private AudioManager audioManager;
    private GestureDetector gestureDetector;
    
    private ImageButton btnNext, btnPrevious, btnBack, btnFavorite;
    private TextView tvTitle;
    
    private long channelId;
    private long playlistId;
    private List<Channel> channelList = new ArrayList<>();
    private int currentIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playerView = findViewById(R.id.player_view);
        pbLoading = findViewById(R.id.pb_loading);
        tvGestureStatus = findViewById(R.id.tv_gesture_status);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Media3 PlayerView automatically inflates custom_exo_controls.xml if specified in XML.
        // We find the sub-views directly from playerView to hook our custom logic.
        btnNext = playerView.findViewById(R.id.btn_next);
        btnPrevious = playerView.findViewById(R.id.btn_previous);
        btnBack = playerView.findViewById(R.id.btn_back);
        btnFavorite = playerView.findViewById(R.id.btn_favorite);
        tvTitle = playerView.findViewById(R.id.tv_title);

        channelId = getIntent().getLongExtra(EXTRA_CHANNEL_ID, -1);
        playlistId = getIntent().getLongExtra(EXTRA_PLAYLIST_ID, -1);
        String initialUrl = getIntent().getStringExtra(EXTRA_URL);
        String initialName = getIntent().getStringExtra(EXTRA_NAME);

        setupExoPlayer();
        setupControlListeners();
        setupGestures();
        
        if (initialUrl != null) {
            playStream(initialUrl, initialName);
        }
        
        loadPlaylistAndSetIndex();
    }

    private void setupExoPlayer() {
        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("DekhiPlayer/1.0")
                .setAllowCrossProtocolRedirects(true);

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();
        
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                pbLoading.setVisibility(playbackState == Player.STATE_BUFFERING ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPlayerError(androidx.media3.common.PlaybackException error) {
                Log.e("IPTV_DEBUG", "ExoPlayer Error: " + error.getMessage());
                showErrorDialog();
            }
        });
    }

    private void playChannel(int index) {
        if (channelList == null || channelList.isEmpty()) return;

        if (index >= 0 && index < channelList.size()) {
            currentIndex = index;
            Channel channel = channelList.get(currentIndex);
            channelId = channel.getId();
            playStream(channel.getStreamUrl(), channel.getName());
            updateFavoriteIcon();
            updateHistory();
        } else {
            Toast.makeText(this, index < 0 ? "Beginning of playlist" : "End of playlist", Toast.LENGTH_SHORT).show();
        }
    }

    private void playStream(String url, String name) {
        if (url == null) return;
        if (tvTitle != null) tvTitle.setText(name);
        
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(url))
                .setMimeType(url.contains(".m3u8") ? MimeTypes.APPLICATION_M3U8 : null)
                .build();
        
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    private void setupControlListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> playChannel(currentIndex + 1));
        }

        if (btnPrevious != null) {
            btnPrevious.setOnClickListener(v -> playChannel(currentIndex - 1));
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> toggleFavorite());
        }
    }

    private void loadPlaylistAndSetIndex() {
        if (playlistId != -1) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                List<Channel> channels = db.channelDao().getChannelsByPlaylistSync(playlistId);
                if (channels != null && !channels.isEmpty()) {
                    channelList = channels;
                    for (int i = 0; i < channelList.size(); i++) {
                        if (channelList.get(i).getId() == channelId) {
                            currentIndex = i;
                            break;
                        }
                    }
                    runOnUiThread(this::updateFavoriteIcon);
                }
            }).start();
        }
    }

    private void toggleFavorite() {
        if (channelId == -1) return;
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Channel channel = db.channelDao().getChannelSync(channelId);
            if (channel != null) {
                channel.setFavorite(!channel.isFavorite());
                db.channelDao().update(channel);
                runOnUiThread(this::updateFavoriteIcon);
            }
        }).start();
    }

    private void updateFavoriteIcon() {
        if (channelId == -1 || btnFavorite == null) return;
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Channel channel = db.channelDao().getChannelSync(channelId);
            if (channel != null) {
                runOnUiThread(() -> btnFavorite.setImageResource(channel.isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off));
            }
        }).start();
    }

    private void updateHistory() {
        if (channelId == -1) return;
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Channel channel = db.channelDao().getChannelSync(channelId);
            if (channel != null) {
                channel.setLastWatched(System.currentTimeMillis());
                db.channelDao().update(channel);
            }
        }).start();
    }

    private void showErrorDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Playback Error")
                .setMessage("Sorry, this channel is currently unreachable. Please try another channel!")
                .setCancelable(false)
                .setPositiveButton("NEXT CHANNEL", (dialog, which) -> playChannel(currentIndex + 1))
                .setNegativeButton("OK / BACK", (dialog, which) -> finish())
                .show();
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float deltaY = e1.getY() - e2.getY();
                if (e1.getX() < playerView.getWidth() / 2f) {
                    adjustBrightness(deltaY / playerView.getHeight());
                } else {
                    adjustVolume(deltaY / playerView.getHeight());
                }
                return true;
            }
        });

        playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                tvGestureStatus.setVisibility(View.GONE);
            }
            return false; // Return false to let PlayerView handle its own touch for controls
        });
    }

    private void adjustVolume(float percent) {
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int delta = (int) (percent * maxVol);
        int newVol = Math.max(0, Math.min(maxVol, currentVol + delta));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);
        showGestureStatus("Volume: " + (newVol * 100 / maxVol) + "%");
    }

    private void adjustBrightness(float percent) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float brightness = lp.screenBrightness;
        if (brightness < 0) brightness = 0.5f;
        lp.screenBrightness = Math.max(0.01f, Math.min(1.0f, brightness + percent));
        getWindow().setAttributes(lp);
        showGestureStatus("Brightness: " + (int) (lp.screenBrightness * 100) + "%");
    }

    private void showGestureStatus(String text) {
        tvGestureStatus.setText(text);
        tvGestureStatus.setVisibility(View.VISIBLE);
        if (playerView != null) playerView.showController();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            enterPictureInPictureMode(new android.app.PictureInPictureParams.Builder().build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, android.content.res.Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        playerView.setUseController(!isInPictureInPictureMode);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) player.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
