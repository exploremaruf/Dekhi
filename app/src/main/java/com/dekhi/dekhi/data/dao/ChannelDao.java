package com.dekhi.dekhi.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dekhi.dekhi.data.entity.Channel;

import java.util.List;

@Dao
public interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Channel> channels);

    @Update
    void update(Channel channel);

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    LiveData<List<Channel>> getChannelsByPlaylist(long playlistId);

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    List<Channel> getChannelsByPlaylistSync(long playlistId);

    @Query("SELECT * FROM channels WHERE id = :id")
    Channel getChannelSync(long id);

    @Query("SELECT * FROM channels WHERE isFavorite = 1")
    LiveData<List<Channel>> getFavoriteChannels();

    @Query("SELECT * FROM channels WHERE lastWatched > 0 ORDER BY lastWatched DESC LIMIT 20")
    LiveData<List<Channel>> getRecentChannels();

    @Query("SELECT * FROM channels WHERE name LIKE :query OR category LIKE :query OR streamUrl LIKE :query")
    LiveData<List<Channel>> searchChannels(String query);
    
    @Query("SELECT DISTINCT category FROM channels WHERE playlistId = :playlistId")
    LiveData<List<String>> getCategories(long playlistId);

    @Query("SELECT DISTINCT category FROM channels WHERE category != 'Uncategorized' ORDER BY category ASC")
    LiveData<List<String>> getAllCategories();

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    void deleteByPlaylistId(long playlistId);

    @Query("UPDATE channels SET lastWatched = 0")
    void clearAllHistory();
}
