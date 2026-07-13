package com.dekhi.dekhi.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.dekhi.dekhi.data.entity.Playlist;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Insert
    long insert(Playlist playlist);

    @Update
    void update(Playlist playlist);

    @Delete
    void delete(Playlist playlist);

    @Query("SELECT * FROM playlists WHERE isPending = 0 ORDER BY lastImported DESC")
    LiveData<List<Playlist>> getAllPlaylists();

    @Query("SELECT * FROM playlists WHERE id = :id")
    Playlist getPlaylistById(long id);

    @Query("SELECT * FROM playlists WHERE name LIKE :query")
    LiveData<List<Playlist>> searchPlaylists(String query);
}
