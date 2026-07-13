package com.dekhi.dekhi.ui.player;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import com.dekhi.dekhi.data.PlaylistRepository;
import com.dekhi.dekhi.data.entity.Channel;
import java.util.List;

@OptIn(markerClass = UnstableApi.class)
public class PlayerViewModel extends AndroidViewModel {
    private final PlaylistRepository repository;
    private final MutableLiveData<List<Channel>> channelList = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(-1);
    private final MutableLiveData<Channel> currentChannel = new MutableLiveData<>();
    private ExoPlayer player;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        repository = new PlaylistRepository(application);
        setupPlayer(application);
    }

    private void setupPlayer(Application app) {
        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("DekhiPlayer/1.0")
                .setAllowCrossProtocolRedirects(true);

        player = new ExoPlayer.Builder(app)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    public void loadPlaylist(long playlistId, long initialChannelId) {
        repository.getChannelsByPlaylistSync(playlistId, channels -> {
            channelList.setValue(channels);
            if (channels != null) {
                for (int i = 0; i < channels.size(); i++) {
                    if (channels.get(i).getId() == initialChannelId) {
                        currentIndex.setValue(i);
                        currentChannel.setValue(channels.get(i));
                        updateHistory(channels.get(i));
                        break;
                    }
                }
            }
        });
    }

    public LiveData<Channel> getCurrentChannel() {
        return currentChannel;
    }

    public LiveData<List<Channel>> getChannelList() {
        return channelList;
    }

    public void playNext() {
        List<Channel> list = channelList.getValue();
        Integer index = currentIndex.getValue();
        if (list != null && index != null && index < list.size() - 1) {
            int nextIndex = index + 1;
            currentIndex.setValue(nextIndex);
            currentChannel.setValue(list.get(nextIndex));
            updateHistory(list.get(nextIndex));
        }
    }

    public void playPrevious() {
        List<Channel> list = channelList.getValue();
        Integer index = currentIndex.getValue();
        if (list != null && index != null && index > 0) {
            int prevIndex = index - 1;
            currentIndex.setValue(prevIndex);
            currentChannel.setValue(list.get(prevIndex));
            updateHistory(list.get(prevIndex));
        }
    }

    public void toggleFavorite() {
        Channel channel = currentChannel.getValue();
        if (channel != null) {
            channel.setFavorite(!channel.isFavorite());
            repository.updateChannel(channel);
            currentChannel.setValue(channel);
        }
    }

    private void updateHistory(Channel channel) {
        channel.setLastWatched(System.currentTimeMillis());
        repository.updateChannel(channel);
    }
}
