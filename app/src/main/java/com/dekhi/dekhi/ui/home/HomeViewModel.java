package com.dekhi.dekhi.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.dekhi.dekhi.data.PlaylistRepository;
import com.dekhi.dekhi.data.entity.Channel;
import com.dekhi.dekhi.data.entity.Playlist;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final PlaylistRepository repository;
    private final LiveData<List<Playlist>> allPlaylists;
    private final LiveData<List<Channel>> recentChannels;
    
    // Search state
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<Channel>> searchResults;
    private final LiveData<List<Playlist>> playlistSearchResults;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new PlaylistRepository(application);
        
        // These are backed by Room LiveData, so they auto-update when the database changes
        allPlaylists = repository.getAllPlaylists();
        recentChannels = repository.getRecent();
        
        // Reactive search logic: triggers repository search whenever searchQuery changes
        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return new MutableLiveData<>();
            }
            return repository.searchChannels(query);
        });

        playlistSearchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return repository.getAllPlaylists();
            }
            return repository.searchPlaylists(query);
        });
    }

    public LiveData<List<Playlist>> getPlaylistSearchResults() {
        return playlistSearchResults;
    }

    public LiveData<List<Playlist>> getAllPlaylists() {
        return allPlaylists;
    }

    public LiveData<List<Channel>> getRecentChannels() {
        return recentChannels;
    }

    public LiveData<List<Channel>> getFavorites() {
        return repository.getFavorites();
    }

    public LiveData<List<Channel>> getChannelsForPlaylist(long playlistId) {
        return repository.getChannelsForPlaylist(playlistId);
    }

    public LiveData<List<Channel>> getChannelsFiltered(long playlistId, String category, String query) {
        return repository.getChannelsFiltered(playlistId, category, query);
    }

    public LiveData<List<String>> getCategories(long playlistId) {
        return repository.getCategories(playlistId);
    }

    public LiveData<List<String>> getAllCategories() {
        return repository.getAllCategories();
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<List<Channel>> getSearchResults() {
        return searchResults;
    }

    public void importPlaylist(String name, String url, PlaylistRepository.ImportCallback callback) {
        // Trigger repository to fetch and parse on background thread
        repository.importPlaylist(name, url, callback);
    }

    public void deletePlaylist(Playlist playlist) {
        repository.deletePlaylist(playlist);
    }

    public void clearHistory() {
        repository.clearHistory();
    }
}
