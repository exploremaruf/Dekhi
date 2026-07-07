package com.dekhi.dekhi.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlists")
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String url;
    private long lastImported;
    private String channelPreviewSnippet;

    public Playlist(String name, String url, long lastImported) {
        this.name = name;
        this.url = url;
        this.lastImported = lastImported;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public long getLastImported() { return lastImported; }
    public void setLastImported(long lastImported) { this.lastImported = lastImported; }
    public String getChannelPreviewSnippet() { return channelPreviewSnippet; }
    public void setChannelPreviewSnippet(String channelPreviewSnippet) { this.channelPreviewSnippet = channelPreviewSnippet; }
}
