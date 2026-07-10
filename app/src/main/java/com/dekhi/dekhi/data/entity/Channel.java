package com.dekhi.dekhi.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "channels",
    foreignKeys = @ForeignKey(
        entity = Playlist.class,
        parentColumns = "id",
        childColumns = "playlistId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("playlistId"),
        @Index("category")
    }
)
public class Channel {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long playlistId;
    private String name;
    private String logoUrl;
    private String streamUrl;
    private String category;
    private boolean isFavorite;
    private long lastWatched;

    public Channel(long playlistId, String name, String logoUrl, String streamUrl, String category) {
        this.playlistId = playlistId;
        this.name = name;
        this.logoUrl = logoUrl;
        this.streamUrl = streamUrl;
        this.category = category;
        this.isFavorite = false;
        this.lastWatched = 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getPlaylistId() { return playlistId; }
    public void setPlaylistId(long playlistId) { this.playlistId = playlistId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public long getLastWatched() { return lastWatched; }
    public void setLastWatched(long lastWatched) { this.lastWatched = lastWatched; }
}
