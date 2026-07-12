package com.dekhi.dekhi.data;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.dekhi.dekhi.data.dao.ChannelDao;
import com.dekhi.dekhi.data.dao.PlaylistDao;
import com.dekhi.dekhi.data.entity.Channel;
import com.dekhi.dekhi.data.entity.Playlist;
import com.dekhi.dekhi.util.M3UParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistRepository {
    private static final String TAG = "PlaylistRepository";
    private final PlaylistDao playlistDao;
    private final ChannelDao channelDao;
    private final ExecutorService executorService;
    private final Application application;

    public PlaylistRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getInstance(application);
        playlistDao = db.playlistDao();
        channelDao = db.channelDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Playlist>> getAllPlaylists() {
        return playlistDao.getAllPlaylists();
    }

    public LiveData<List<Channel>> getChannelsForPlaylist(long playlistId) {
        return channelDao.getChannelsByPlaylist(playlistId);
    }

    public LiveData<List<Channel>> getChannelsFiltered(long playlistId, String category, String query) {
        return channelDao.getChannelsFiltered(playlistId, category, "%" + query + "%");
    }

    public LiveData<List<String>> getCategories(long playlistId) {
        return channelDao.getCategories(playlistId);
    }

    public LiveData<List<String>> getAllCategories() {
        return channelDao.getAllCategories();
    }

    public LiveData<List<Channel>> searchChannels(String query) {
        return channelDao.searchChannels("%" + query + "%");
    }

    public LiveData<List<Playlist>> searchPlaylists(String query) {
        return playlistDao.searchPlaylists("%" + query + "%");
    }

    public LiveData<List<Channel>> getFavorites() {
        return channelDao.getFavoriteChannels();
    }

    public LiveData<List<Channel>> getRecent() {
        return channelDao.getRecentChannels();
    }

    public void importPlaylist(String name, String url, ImportCallback callback) {
        final String trimmedUrl = url.trim();
        executorService.execute(() -> {
            InputStream inputStream = null;
            HttpURLConnection connection = null;
            try {
                if (trimmedUrl.startsWith("http")) {
                    URL requestUrl = new URL(trimmedUrl);
                    int redirects = 0;
                    boolean connected = false;

                    while (redirects < 5 && !connected) {
                        connection = (HttpURLConnection) requestUrl.openConnection();
                        connection.setConnectTimeout(20000);
                        connection.setReadTimeout(30000);
                        connection.setInstanceFollowRedirects(true);
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

                        int status = connection.getResponseCode();
                        Log.d("IPTV_DEBUG", "Network: URL=" + requestUrl + ", Status=" + status);

                        if (status >= 300 && status <= 308 && status != 304) {
                            String location = connection.getHeaderField("Location");
                            if (location == null) throw new Exception("Redirect with no location");
                            requestUrl = new URL(requestUrl, location);
                            connection.disconnect();
                            redirects++;
                            Log.d("IPTV_DEBUG", "Network: Redirecting to=" + location);
                        } else if (status == HttpURLConnection.HTTP_OK) {
                            connected = true;
                        } else {
                            throw new Exception("HTTP Error: " + status);
                        }
                    }

                    if (!connected) throw new Exception("Too many redirects");
                    int contentLength = connection.getContentLength();
                    Log.d("IPTV_DEBUG", "Network: Connected. Content Length=" + contentLength);
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = application.getContentResolver().openInputStream(Uri.parse(trimmedUrl));
                }

                if (inputStream == null) throw new Exception("Stream is null");

                Playlist playlist = new Playlist(name, trimmedUrl, System.currentTimeMillis());
                long playlistId = playlistDao.insert(playlist);

                Log.d("IPTV_DEBUG", "DB: Starting batch import for playlist ID: " + playlistId);
                M3UParser.ParseResult result = M3UParser.parse(inputStream, playlistId, batch -> {
                    Log.d("IPTV_DEBUG", "DB: Inserting batch of " + batch.size() + " channels...");
                    channelDao.insertAll(batch);
                });

                if (result.channelCount == 0) {
                    playlistDao.delete(playlist);
                    throw new Exception("No valid channels found in this playlist.");
                }

                playlist.setId(playlistId);
                playlist.setChannelPreviewSnippet(result.previewSnippet);
                playlist.setChannelCount(result.channelCount);
                playlist.setGroupCount(result.groupCount);
                playlistDao.update(playlist);
                
                Log.d("IPTV_DEBUG", "DB: Import completed. Total channels: " + result.channelCount);

                new Handler(Looper.getMainLooper()).post(callback::onSuccess);

            } catch (Exception e) {
                Log.e(TAG, "Import failed", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception ignored) {}
            }
        });
    }

    public void deletePlaylist(Playlist playlist) {
        executorService.execute(() -> playlistDao.delete(playlist));
    }

    public void clearHistory() {
        executorService.execute(channelDao::clearAllHistory);
    }

    public void updateChannel(Channel channel) {
        executorService.execute(() -> channelDao.update(channel));
    }

    public void getChannelsByPlaylistSync(long playlistId, DataCallback<List<Channel>> callback) {
        executorService.execute(() -> {
            List<Channel> channels = channelDao.getChannelsByPlaylistSync(playlistId);
            new Handler(Looper.getMainLooper()).post(() -> callback.onDataLoaded(channels));
        });
    }

    public void getChannelSync(long channelId, DataCallback<Channel> callback) {
        executorService.execute(() -> {
            Channel channel = channelDao.getChannelSync(channelId);
            new Handler(Looper.getMainLooper()).post(() -> callback.onDataLoaded(channel));
        });
    }

    public interface ImportCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }
}
