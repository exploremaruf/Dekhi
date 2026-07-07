package com.dekhi.dekhi.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.dekhi.dekhi.data.dao.ChannelDao;
import com.dekhi.dekhi.data.dao.PlaylistDao;
import com.dekhi.dekhi.data.entity.Channel;
import com.dekhi.dekhi.data.entity.Playlist;

@Database(entities = {Playlist.class, Channel.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract PlaylistDao playlistDao();
    public abstract ChannelDao channelDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "dekhi_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
